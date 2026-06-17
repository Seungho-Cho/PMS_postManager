package com.mason.api.post.dto;

import com.mason.api.post.entity.Post;

public record UpdatePostRequest(String title, String caption, Post.PostStatus status) {
}