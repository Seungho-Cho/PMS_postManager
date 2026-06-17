package com.mason.api.post.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * Discord 봇/Kafka 연동 전, 웹에서 테스트용 Post를 직접 생성하기 위한 요청.
 * photoUrls는 R2 업로드 전이라 원본/썸네일에 동일한 URL을 사용한다.
 */
public record CreatePostRequest(
    @NotBlank String discordMessageId,
    @NotBlank String authorDiscordId,
    @NotBlank String authorDiscordIcon,
    @NotBlank String authorDiscordNickname,
    String title,
    String caption,
    List<String> photoUrls
) {
}