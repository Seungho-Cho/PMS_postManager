package com.mason.api.discordbot;

import com.mason.api.config.AppConfigService;
import com.mason.common.event.DiscordBotAction;
import com.mason.common.event.DiscordBotConnectCommand;
import com.mason.common.event.KafkaTopics;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * DB({@link com.mason.api.config.entity.AppConfig})에 저장된 봇 토큰/감시 채널ID를 읽어
 * discord-bot에게 시작/종료/재시작을 지시하는 {@link DiscordBotConnectCommand}를 발행한다.
 */
@Service
public class DiscordBotControlService {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotControlService.class);

    private static final String BOT_TOKEN_KEY = "discord.bot.token";
    private static final String WATCH_CHANNEL_ID_KEY = "discord.watch-channel-id";

    private final AppConfigService appConfigService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DiscordBotControlService(AppConfigService appConfigService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.appConfigService = appConfigService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void start() {
        String botToken = appConfigService.findByKey(BOT_TOKEN_KEY).getValue();
        String watchChannelId = appConfigService.findByKey(WATCH_CHANNEL_ID_KEY).getValue();
        send(new DiscordBotConnectCommand(DiscordBotAction.START, botToken, watchChannelId));
    }

    public void stop() {
        send(new DiscordBotConnectCommand(DiscordBotAction.STOP, null, null));
    }

    public void restart() {
        String botToken = appConfigService.findByKey(BOT_TOKEN_KEY).getValue();
        String watchChannelId = appConfigService.findByKey(WATCH_CHANNEL_ID_KEY).getValue();
        send(new DiscordBotConnectCommand(DiscordBotAction.RESTART, botToken, watchChannelId));
    }

    /**
     * api 재시작 시 discord-bot이 마지막 설정을 다시 받을 수 있도록 한 번 더 발행한다.
     * 설정이 아직 등록되지 않았다면(최초 구동 등) 조용히 넘어간다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void republishOnStartup() {
        try {
            start();
        } catch (EntityNotFoundException e) {
            log.info("discord-bot 설정이 아직 등록되지 않아 시작 커맨드를 발행하지 않습니다.");
        }
    }

    private void send(DiscordBotConnectCommand command) {
        kafkaTemplate.send(KafkaTopics.DISCORD_BOT_CONNECT_COMMAND, command);
    }
}