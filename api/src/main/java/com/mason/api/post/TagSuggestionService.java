package com.mason.api.post;

import java.util.Arrays;
import java.util.List;

import com.mason.api.config.AppConfigService;
import com.mason.api.groq.GroqClient;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 제목/캡션을 바탕으로 Groq에 SNS 해시태그 추천을 요청한다.
 */
@Service
public class TagSuggestionService {

    private static final String GROQ_API_KEY_CONFIG = "groq.api.key";
    private static final String GROQ_MODEL_CONFIG = "groq.llm.model";
    private static final String GROQ_BASE_PROMPT_CONFIG = "groq.base.prompt";

    /** 태그 파싱이 깨지지 않도록 출력 형식만 강제하는 시스템 프롬프트. 추천 내용 자체는 groq.base.prompt(DB)로 관리한다. */
    private static final String FORMAT_INSTRUCTION = """
        다른 설명 없이 해시태그만 공백으로 구분해서 한 줄로 출력해. 각 태그는 #으로 시작해야 해.
        """;

    private final AppConfigService appConfigService;
    private final GroqClient groqClient;

    public TagSuggestionService(AppConfigService appConfigService, GroqClient groqClient) {
        this.appConfigService = appConfigService;
        this.groqClient = groqClient;
    }

    public List<String> suggest(String title, String caption) {
        String apiKey = appConfigService.findByKey(GROQ_API_KEY_CONFIG).getValue();
        String model = appConfigService.findByKey(GROQ_MODEL_CONFIG).getValue();
        String basePrompt = appConfigService.findByKey(GROQ_BASE_PROMPT_CONFIG).getValue();

        String userPrompt = basePrompt + "\n제목: " + nullToEmpty(title) + "\n캡션: " + nullToEmpty(caption);

        String content = groqClient.complete(apiKey, model, FORMAT_INSTRUCTION, userPrompt);

        return Arrays.stream(content.trim().split("\\s+"))
            .filter(StringUtils::hasText)
            .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
            .toList();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}