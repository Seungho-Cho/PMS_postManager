package com.mason.common.event;

/**
 * api와 discord-bot이 공유하는 Kafka 토픽 이름.
 */
public final class KafkaTopics {

    /** api -> discord-bot: {@link DiscordBotConnectCommand} */
    public static final String DISCORD_BOT_CONNECT_COMMAND = "discord-bot.connect.command";

    /** discord-bot -> api: {@link PhotoUploadRequested} */
    public static final String PHOTO_UPLOAD_REQUESTED = "photo.upload.requested";

    /** api -> discord-bot: {@link PostCreated} */
    public static final String POST_CREATED = "post.created";

    private KafkaTopics() {
    }
}