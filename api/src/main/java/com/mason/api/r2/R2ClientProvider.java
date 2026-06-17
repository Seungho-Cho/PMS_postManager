package com.mason.api.r2;

import com.mason.api.config.AppConfigService;

import org.springframework.stereotype.Component;

/**
 * DB({@link com.mason.api.config.entity.AppConfig})에 저장된 R2 자격증명으로 {@link R2Client}를 만든다.
 */
@Component
public class R2ClientProvider {

    private static final String ENDPOINT_KEY = "r2.endpoint";
    private static final String ACCESS_KEY_KEY = "r2.access-key";
    private static final String SECRET_KEY_KEY = "r2.secret-key";
    private static final String BUCKET_KEY = "r2.bucket";
    private static final String PUBLIC_BASE_URL_KEY = "r2.public-base-url";

    private final AppConfigService appConfigService;

    public R2ClientProvider(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    public R2Client getClient() {
        return new R2Client(
            appConfigService.findByKey(ENDPOINT_KEY).getValue(),
            appConfigService.findByKey(ACCESS_KEY_KEY).getValue(),
            appConfigService.findByKey(SECRET_KEY_KEY).getValue(),
            appConfigService.findByKey(BUCKET_KEY).getValue(),
            appConfigService.findByKey(PUBLIC_BASE_URL_KEY).getValue()
        );
    }
}