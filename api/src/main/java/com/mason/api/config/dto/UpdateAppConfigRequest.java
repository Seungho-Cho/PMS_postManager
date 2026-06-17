package com.mason.api.config.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppConfigRequest(
    @NotBlank String value
) {
}