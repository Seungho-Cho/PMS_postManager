package com.mason.api.config.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAppConfigRequest(
    @NotBlank String key,
    @NotBlank String value
) {
}