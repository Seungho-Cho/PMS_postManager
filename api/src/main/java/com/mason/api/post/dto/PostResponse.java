package com.mason.api.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.mason.api.post.entity.Post;

public record PostResponse(
    Long id,
    String discordMessageId,
    String authorDiscordId,
    String authorDiscordIcon,
    String authorDiscordNickname,
    String title,
    String caption,
    Post.PostStatus status,
    LocalDateTime postedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<PhotoResponse> photos
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getDiscordMessageId(),
            post.getAuthorDiscordId(),
            post.getAuthorDiscordIcon(),
            post.getAuthorDiscordNickname(),
            post.getTitle(),
            post.getCaption(),
            post.getStatus(),
            post.getPostedAt(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getPhotos().stream().map(PhotoResponse::from).toList()
        );
    }
}