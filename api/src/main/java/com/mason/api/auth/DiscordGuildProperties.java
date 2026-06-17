package com.mason.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.discord")
public record DiscordGuildProperties(String guildId) {
}