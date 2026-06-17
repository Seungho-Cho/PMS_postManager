package com.mason.discordbot.listener;

import com.mason.common.event.KafkaTopics;
import com.mason.common.event.PostCreated;
import com.mason.discordbot.connection.DiscordBotConnectionManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * api가 Post 생성을 끝내고 발행한 {@link PostCreated}를 받아, 원본 메시지에 봇이 남긴
 * "이미지 처리중" 답글을 찾아 "포스트 수정하기" 링크 버튼으로 교체한다.
 */
@Component
public class PostCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(PostCreatedListener.class);
    private static final int HISTORY_SEARCH_LIMIT = 50;
    private static final int COLOR_SUCCESS = 0x57F287;

    private final DiscordBotConnectionManager connectionManager;

    public PostCreatedListener(DiscordBotConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @KafkaListener(topics = KafkaTopics.POST_CREATED)
    public void onPostCreated(PostCreated event) {
        JDA jda = connectionManager.getCurrentJda();
        if (jda == null) {
            log.warn("discord-bot이 연결되어 있지 않아 버튼을 달지 못했습니다. discordMessageId={}", event.discordMessageId());
            return;
        }

        MessageChannel channel = jda.getChannelById(MessageChannel.class, event.discordChannelId());
        if (channel == null) {
            log.warn("채널을 찾지 못했습니다. channelId={}", event.discordChannelId());
            return;
        }

        channel.getHistoryAfter(event.discordMessageId(), HISTORY_SEARCH_LIMIT).queue(history -> {
            Message processingReply = history.getRetrievedHistory().stream()
                .filter(message -> message.getAuthor().getId().equals(jda.getSelfUser().getId()))
                .filter(message -> message.getReferencedMessage() != null)
                .filter(message -> message.getReferencedMessage().getId().equals(event.discordMessageId()))
                .findFirst()
                .orElse(null);

            if (processingReply == null) {
                log.warn("처리중 답글을 찾지 못했습니다. discordMessageId={}", event.discordMessageId());
                return;
            }

            MessageEmbed successEmbed = new EmbedBuilder()
                .setTitle("✅  포스트 생성 완료")
                .setDescription("이미지 업로드가 완료됐어요. 아래 버튼으로 바로 포스트를 작성해보세요.")
                .setColor(COLOR_SUCCESS)
                .setFooter("PostManager")
                .setTimestamp(java.time.Instant.now())
                .build();

            channel.editMessageEmbedsById(processingReply.getId(), successEmbed)
                .setActionRow(Button.link(event.postEditUrl(), "✏️  포스트 작성하기"))
                .queue();
        });
    }
}
