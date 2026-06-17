package com.mason.api.post;

import java.util.List;

import com.mason.api.post.dto.CreatePostRequest;
import com.mason.api.post.entity.Photo;
import com.mason.api.post.entity.Post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

/**
 * 포스트({@link Post})에 대한 조회/생성/수정/삭제를 담당한다.
 * 정식 생성 경로는 Discord 봇이 발행하는 Kafka 이벤트지만, 아직 연동되지 않아
 * {@link #createForTest}로 테스트용 Post를 직접 생성할 수 있다.
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
    }

    @Transactional
    public Post createForTest(CreatePostRequest request) {
        if (postRepository.existsByDiscordMessageId(request.discordMessageId())) {
            throw new EntityExistsException(request.discordMessageId());
        }

        Post post = new Post(
            request.discordMessageId(),
            request.authorDiscordId(),
            request.authorDiscordIcon(),
            request.authorDiscordNickname()
        );
        post.updateContent(request.title(), request.caption());

        if (!CollectionUtils.isEmpty(request.photoUrls())) {
            request.photoUrls().forEach(url -> post.addPhoto(new Photo(post, url, url)));
        }

        return postRepository.save(post);
    }

    @Transactional
    public void updateContent(Long id, String title, String caption, Post.PostStatus status) {
        Post post = findById(id);
        post.updateContent(title, caption);
        post.changeStatus(status);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}