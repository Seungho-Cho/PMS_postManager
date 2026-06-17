package com.mason.common.event;

import java.util.List;

/**
 * discord-bot이 감시 채널에서 이미지 첨부 메시지를 감지했을 때 발행하는 이벤트.
 * 한 메시지(discordMessageId)에 첨부된 이미지가 여러 장이면 카루셀로 간주한다.
 */
public record PhotoUploadRequested(
    String discordMessageId,
    String discordChannelId,
    String uploaderDiscordId,
    String uploaderDiscordNickname,
    String uploaderDiscordIconUrl,
    List<String> attachmentUrls
) {
}