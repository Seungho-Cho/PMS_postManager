package com.mason.api.instagram;

import java.time.LocalDateTime;

import com.mason.api.config.AppConfigService;
import com.mason.api.config.entity.AppConfig;
import com.mason.api.instagram.dto.InstagramStatusResponse;

import org.springframework.stereotype.Service;

/**
 * DB에 저장된 Instagram access token의 상태 확인 및 갱신을 담당한다.
 * 자동 갱신(스케줄러)은 차후 구현 예정 — 지금은 수동 호출만 지원한다.
 */
@Service
public class InstagramKeyService {

    private static final String ACCESS_TOKEN_CONFIG = "instagram.api.access.token";

    /** Instagram 장기 토큰의 실제 만료일은 조회 API가 없어, 마지막 갱신일 기준 60일을 추정치로 보여준다. */
    private static final long LONG_LIVED_TOKEN_DAYS = 60;

    private final AppConfigService appConfigService;
    private final InstagramClient instagramClient;

    public InstagramKeyService(AppConfigService appConfigService, InstagramClient instagramClient) {
        this.appConfigService = appConfigService;
        this.instagramClient = instagramClient;
    }

    public InstagramStatusResponse checkStatus() {
        AppConfig config = appConfigService.findByKey(ACCESS_TOKEN_CONFIG);
        InstagramClient.TokenCheckResult result = instagramClient.checkToken(config.getValue());
        return buildResponse(result.valid(), result.message(), config.getUpdatedAt());
    }

    public InstagramStatusResponse refresh() {
        AppConfig config = appConfigService.findByKey(ACCESS_TOKEN_CONFIG);
        InstagramClient.RefreshTokenResult result = instagramClient.refreshToken(config.getValue());

        if (!result.success()) {
            return buildResponse(false, result.message(), config.getUpdatedAt());
        }

        appConfigService.updateValue(ACCESS_TOKEN_CONFIG, result.newAccessToken());
        AppConfig updated = appConfigService.findByKey(ACCESS_TOKEN_CONFIG);
        return buildResponse(true, "토큰을 갱신했습니다.", updated.getUpdatedAt());
    }

    private InstagramStatusResponse buildResponse(boolean valid, String message, LocalDateTime lastUpdatedAt) {
        return new InstagramStatusResponse(valid, message, lastUpdatedAt, lastUpdatedAt.plusDays(LONG_LIVED_TOKEN_DAYS));
    }
}