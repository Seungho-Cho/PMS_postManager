package com.mason.api.instagram.dto;

import java.time.LocalDateTime;

public record InstagramStatusResponse(
    boolean valid,
    String message,
    LocalDateTime lastUpdatedAt,
    LocalDateTime estimatedExpiresAt
) {
}