package com.mason.api.groq;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Groq Chat Completions API(OpenAI 호환)를 호출하는 클라이언트.
 * API 키는 DB({@link com.mason.api.config.entity.AppConfig})에 저장되어 바뀔 수 있어 호출 시점에 매번 전달받는다.
 */
@Component
public class GroqClient {

    private final RestClient restClient = RestClient.create("https://api.groq.com/openai/v1");

    public String complete(String apiKey, String model, String systemPrompt, String userPrompt) {
        GroqChatResponse response = restClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of(
                "model", model,
                "temperature", 0.1,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
                )
            ))
            .retrieve()
            .body(GroqChatResponse.class);

        return response.choices().get(0).message().content();
    }
}