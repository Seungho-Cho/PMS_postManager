package com.mason.api.post;

import java.util.List;

import com.mason.api.post.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    boolean existsByDiscordMessageId(String discordMessageId);
}