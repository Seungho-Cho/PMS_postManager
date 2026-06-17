package com.mason.api.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Instagram API with Instagram Login 클라이언트.
 * graph.instagram.com에 자기 정보 조회(/me)를 보내 성공 여부로 유효성을 판단한다.
 * Access Token은 DB({@link com.mason.api.config.entity.AppConfig})에 저장되어 바뀔 수 있어 호출 시점에 매번 전달받는다.
 */
@Component
public class InstagramClient {

    private final RestClient restClient = RestClient.create("https://graph.instagram.com/v21.0");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenCheckResult checkToken(String accessToken) {
        try {
            restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/me")
                    .queryParam("fields", "id,username")
                    .queryParam("access_token", accessToken)
                    .build())
                .retrieve()
                .toBodilessEntity();
            return new TokenCheckResult(true, null);
        } catch (RestClientResponseException e) {
            return new TokenCheckResult(false, extractErrorMessage(e));
        }
    }

    /**
     * 만료 전 장기 토큰만 갱신 가능하다 (이미 만료된 토큰은 OAuth 재인증부터 다시 해야 한다).
     * 앱시크릿이 필요 없고, 새 60일짜리 토큰을 발급받는다.
     */
    public RefreshTokenResult refreshToken(String accessToken) {
        try {
            RefreshTokenResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/refresh_access_token")
                    .queryParam("grant_type", "ig_refresh_token")
                    .queryParam("access_token", accessToken)
                    .build())
                .retrieve()
                .body(RefreshTokenResponse.class);
            return new RefreshTokenResult(true, response.accessToken(), null);
        } catch (RestClientResponseException e) {
            return new RefreshTokenResult(false, null, extractErrorMessage(e));
        }
    }

    private String extractErrorMessage(RestClientResponseException e) {
        try {
            GraphErrorEnvelope envelope = objectMapper.readValue(e.getResponseBodyAsString(), GraphErrorEnvelope.class);
            return envelope.error().message();
        } catch (Exception parseFailure) {
            return "토큰이 유효하지 않습니다.";
        }
    }

    public record TokenCheckResult(boolean valid, String message) {
    }

    public record RefreshTokenResult(boolean success, String newAccessToken, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RefreshTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GraphErrorEnvelope(GraphError error) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GraphError(String message) {
    }
}
