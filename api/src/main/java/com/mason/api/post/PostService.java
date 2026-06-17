package com.mason.api.post;

import java.util.List;

import com.mason.api.post.entity.Post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

/**
 * 포스트({@link Post})에 대한 조회/수정/삭제를 담당한다.
 * 생성은 discord-bot이 발행하는 Kafka 이벤트({@code PhotoUploadEventListener})를 통해서만 이루어진다.
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
    public void updateContent(
        Long id,
        String title,
        String caption,
        String makerName,
        String makerInstagramId,
        String makerXId,
        Post.PostStatus status
    ) {
        Post post = findById(id);
        post.updateContent(title, caption, makerName, makerInstagramId, makerXId);
        post.changeStatus(status);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}