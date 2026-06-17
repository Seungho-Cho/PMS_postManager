package com.mason.common.event;

/**
 * api가 {@link PhotoUploadRequested}를 처리해 Post 생성을 끝냈을 때
 * discord-bot에게 "포스트 수정하기" 버튼을 달아달라고 알리는 이벤트.
 */
public record PostCreated(
    String discordMessageId,
    String discordChannelId,
    String postEditUrl
) {
}