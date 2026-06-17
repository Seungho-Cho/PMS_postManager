package com.mason.api.post.dto;

import com.mason.api.post.SnsPlatform;

import jakarta.validation.constraints.NotNull;

public record CaptionPreviewRequest(
    String title,
    String caption,
    String makerName,
    String makerInstagramId,
    String makerXId,
    @NotNull SnsPlatform platform
) {
}