package com.mason.api.config.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * R2/Discord/Instagram/X/Groq 등 외부 연동에 필요한 키-값 설정.
 * key는 "discord.bot.token", "r2.access_key" 처럼 네임스페이스 형태로 관리하며 PK로 사용한다.
 */
@Entity
@Table(name = "app_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppConfig {

    @Id
    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(name = "config_value", nullable = false, columnDefinition = "text")
    private String value;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AppConfig(String key, String value) {
        this.key = key;
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 값을 갱신하고 updatedAt을 현재 시각으로 갱신한다.
     */
    public void updateValue(String value) {
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }
}