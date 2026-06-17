package com.mason.api.discordbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * discord-bot 시작/종료/재시작을 지시하는 관리자용 API.
 * 어드민 권한 체크는 아직 없다 (차후 구현).
 */
@RestController
@RequestMapping("/api/discord-bot")
public class DiscordBotControlController {

    private final DiscordBotControlService discordBotControlService;

    public DiscordBotControlController(DiscordBotControlService discordBotControlService) {
        this.discordBotControlService = discordBotControlService;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        discordBotControlService.start();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop() {
        discordBotControlService.stop();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restart")
    public ResponseEntity<Void> restart() {
        discordBotControlService.restart();
        return ResponseEntity.noContent().build();
    }
}