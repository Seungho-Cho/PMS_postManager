package com.mason.discordbot.connection;

import java.util.concurrent.atomic.AtomicReference;

import com.mason.common.event.DiscordBotConnectCommand;
import com.mason.common.event.KafkaTopics;
import com.mason.discordbot.listener.PhotoUploadListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * api로부터 받은 {@link DiscordBotConnectCommand}에 따라 JDA 연결을 시작/종료/재시작한다.
 * discord-bot은 DB에 접근하지 않으므로, 현재 연결 상태는 메모리에만 보존한다(재시작 시 휘발).
 */
@Component
public class DiscordBotConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotConnectionManager.class);

    private final PhotoUploadListener photoUploadListener;
    private final AtomicReference<JDA> currentJda = new AtomicReference<>();

    public DiscordBotConnectionManager(PhotoUploadListener photoUploadListener) {
        this.photoUploadListener = photoUploadListener;
    }

    @KafkaListener(topics = KafkaTopics.DISCORD_BOT_CONNECT_COMMAND)
    public void onConnectCommand(DiscordBotConnectCommand command) {
        switch (command.action()) {
            case START, RESTART -> connect(command.botToken(), command.watchChannelId());
            case STOP -> disconnect();
        }
    }

    private void connect(String botToken, String watchChannelId) {
        disconnect();
        photoUploadListener.setWatchChannelId(watchChannelId);
        try {
            JDA jda = JDABuilder.createDefault(botToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(photoUploadListener)
                .build();
            currentJda.set(jda);
            log.info("discord-bot 연결을 시작했습니다. watchChannelId={}", watchChannelId);
        } catch (IllegalArgumentException e) {
            log.error("discord-bot 토큰이 올바르지 않습니다.", e);
        }
    }

    public JDA getCurrentJda() {
        return currentJda.get();
    }

    private void disconnect() {
        JDA jda = currentJda.getAndSet(null);
        if (jda != null) {
            jda.shutdown();
            log.info("discord-bot 연결을 종료했습니다.");
        }
        photoUploadListener.setWatchChannelId(null);
    }
}