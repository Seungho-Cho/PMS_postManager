package com.mason.api.post.dto;

import com.mason.api.post.entity.Photo;

public record PhotoResponse(Long id, String originalUrl, String thumbnailUrl) {

    public static PhotoResponse from(Photo photo) {
        return new PhotoResponse(photo.getId(), photo.getOriginalUrl(), photo.getThumbnailUrl());
    }
}