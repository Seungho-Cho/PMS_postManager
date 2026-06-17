package com.mason.api.photoupload;

import java.io.ByteArrayOutputStream;

import com.mason.api.post.PostRepository;
import com.mason.api.post.entity.Photo;
import com.mason.api.post.entity.Post;
import com.mason.api.r2.R2Client;
import com.mason.api.r2.R2ClientProvider;
import com.mason.common.event.KafkaTopics;
import com.mason.common.event.PhotoUploadRequested;
import com.mason.common.event.PostCreated;

import net.coobird.thumbnailator.Thumbnails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * discord-bot이 발행한 {@link PhotoUploadRequested}를 구독해 R2 업로드(원본+썸네일) 후
 * {@code Photo}/{@code Post}(DRAFT)를 생성한다.
 */
@Component
public class PhotoUploadEventListener {

    private static final Logger log = LoggerFactory.getLogger(PhotoUploadEventListener.class);
    private static final int THUMBNAIL_SIZE = 400;

    private final PostRepository postRepository;
    private final R2ClientProvider r2ClientProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String baseUrl;
    private final RestClient restClient = RestClient.create();

    public PhotoUploadEventListener(
        PostRepository postRepository,
        R2ClientProvider r2ClientProvider,
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${app.base-url}") String baseUrl
    ) {
        this.postRepository = postRepository;
        this.r2ClientProvider = r2ClientProvider;
        this.kafkaTemplate = kafkaTemplate;
        this.baseUrl = baseUrl;
    }

    @KafkaListener(topics = KafkaTopics.PHOTO_UPLOAD_REQUESTED)
    public void onPhotoUploadRequested(PhotoUploadRequested event) {
        if (postRepository.existsByDiscordMessageId(event.discordMessageId())) {
            log.info("이미 처리된 discordMessageId={}, 건너뜁니다.", event.discordMessageId());
            return;
        }

        R2Client r2Client = r2ClientProvider.getClient();
        Post post = new Post(
            event.discordMessageId(),
            event.uploaderDiscordId(),
            event.uploaderDiscordIconUrl(),
            event.uploaderDiscordNickname()
        );

        int index = 0;
        for (String attachmentUrl : event.attachmentUrls()) {
            post.addPhoto(uploadPhoto(r2Client, post, event.discordMessageId(), index++, attachmentUrl));
        }

        postRepository.save(post);
        log.info("Post 생성 완료: discordMessageId={}, photoCount={}", event.discordMessageId(), post.getPhotos().size());

        String postEditUrl = baseUrl + "/admin/posts/" + post.getId();
        kafkaTemplate.send(KafkaTopics.POST_CREATED, new PostCreated(event.discordMessageId(), event.discordChannelId(), postEditUrl));
    }

    private Photo uploadPhoto(R2Client r2Client, Post post, String discordMessageId, int index, String attachmentUrl) {
        ResponseEntity<byte[]> response = restClient.get().uri(attachmentUrl).retrieve().toEntity(byte[].class);
        byte[] original = response.getBody();
        String contentType = response.getHeaders().getContentType() != null
            ? response.getHeaders().getContentType().toString()
            : MediaType.IMAGE_JPEG_VALUE;
        String extension = extensionOf(contentType);

        byte[] thumbnail = toThumbnail(original);

        String originalUrl = r2Client.upload(
            "photos/%s/%d.%s".formatted(discordMessageId, index, extension),
            original,
            contentType
        );
        String thumbnailUrl = r2Client.upload(
            "thumb/%s/%d.jpg".formatted(discordMessageId, index),
            thumbnail,
            MediaType.IMAGE_JPEG_VALUE
        );

        return new Photo(post, originalUrl, thumbnailUrl);
    }

    private byte[] toThumbnail(byte[] original) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new java.io.ByteArrayInputStream(original))
                .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .outputFormat("jpg")
                .toOutputStream(out);
            return out.toByteArray();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("썸네일 생성에 실패했습니다.", e);
        }
    }

    private String extensionOf(String contentType) {
        if (contentType.contains("png")) {
            return "png";
        }
        if (contentType.contains("gif")) {
            return "gif";
        }
        if (contentType.contains("webp")) {
            return "webp";
        }
        return "jpg";
    }
}
