package com.mason.common.event;

/**
 * api가 discord-bot에게 시작/종료/재시작을 지시하는 커맨드.
 * action이 {@link DiscordBotAction#STOP}이면 botToken/watchChannelId는 사용하지 않는다(null 허용).
 */
public record DiscordBotConnectCommand(
    DiscordBotAction action,
    String botToken,
    String watchChannelId
) {
}