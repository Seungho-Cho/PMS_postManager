package com.mason.discordbot.listener;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.mason.common.event.KafkaTopics;
import com.mason.common.event.PhotoUploadRequested;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 감시 채널(watchChannelId)에 올라온 메시지의 이미지 첨부파일을 감지해 {@link PhotoUploadRequested}를 발행한다.
 * 감시 채널ID는 {@link com.mason.discordbot.connection.DiscordBotConnectionManager}가 커맨드 수신 시 갱신한다.
 */
@Component
public class PhotoUploadListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(PhotoUploadListener.class);

    private static final int COLOR_PROCESSING = 0x5865F2;

    private final AtomicReference<String> watchChannelId = new AtomicReference<>();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PhotoUploadListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void setWatchChannelId(String channelId) {
        watchChannelId.set(channelId);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String channelId = watchChannelId.get();
        if (channelId == null || !event.isFromGuild() || !channelId.equals(event.getChannel().getId())) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }

        List<String> imageUrls = event.getMessage().getAttachments().stream()
            .filter(Attachment::isImage)
            .map(Attachment::getUrl)
            .toList();
        if (imageUrls.isEmpty()) {
            return;
        }

        String discordMessageId = event.getMessageId();
        MessageEmbed processingEmbed = new EmbedBuilder()
            .setTitle("📸  이미지 처리 중")
            .setDescription("업로드된 이미지를 분석하고 있어요. 잠시만 기다려주세요.")
            .setColor(COLOR_PROCESSING)
            .setFooter("PostManager")
            .setTimestamp(java.time.Instant.now())
            .build();
        event.getMessage().replyEmbeds(processingEmbed).queue();

        Member author = event.getMember();
        PhotoUploadRequested requested = new PhotoUploadRequested(
            discordMessageId,
            channelId,
            author.getId(),
            author.getEffectiveName(),
            author.getEffectiveAvatarUrl(),
            imageUrls
        );
        kafkaTemplate.send(KafkaTopics.PHOTO_UPLOAD_REQUESTED, requested);
        log.info("PhotoUploadRequested 발행: discordMessageId={}, imageCount={}", requested.discordMessageId(), imageUrls.size());
    }
}