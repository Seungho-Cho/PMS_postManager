package com.mason.api.config.dto;

import java.time.LocalDateTime;

import com.mason.api.config.entity.AppConfig;

/**
 * {@link AppConfig} 조회 응답.
 */
public record AppConfigResponse(String key, String value, LocalDateTime updatedAt) {

    public static AppConfigResponse from(AppConfig config) {
        return new AppConfigResponse(config.getKey(), config.getValue(), config.getUpdatedAt());
    }
}