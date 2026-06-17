package com.mason.api.post;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 제목 + 캡션 + 제작자 정보를 SNS 플랫폼에 실제로 발행될 텍스트로 조합한다.
 * 미리보기와 실제 발행(추후 구현) 양쪽에서 이 클래스 하나만 사용해야
 * 미리보기와 실제 결과물이 어긋나지 않는다.
 * 순서: 제목 -> 제작자 크레딧 -> 캡션
 */
@Component
public class PostCaptionFormatter {

    public String format(
        SnsPlatform platform,
        String title,
        String caption,
        String makerName,
        String makerInstagramId,
        String makerXId
    ) {
        String creditLine = buildCreditLine(platform, makerName, makerInstagramId, makerXId);

        List<String> parts = new ArrayList<>();
        addIfPresent(parts, title);
        addIfPresent(parts, creditLine);
        addIfPresent(parts, caption);

        return String.join("\n\n", parts);
    }

    private void addIfPresent(List<String> parts, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(value.trim());
        }
    }

    private String buildCreditLine(SnsPlatform platform, String makerName, String makerInstagramId, String makerXId) {
        String snsId = switch (platform) {
            case INSTAGRAM -> makerInstagramId;
            case X -> makerXId;
        };

        if (!StringUtils.hasText(makerName) && !StringUtils.hasText(snsId)) {
            return "";
        }

        StringBuilder credit = new StringBuilder("Build by");
        if (StringUtils.hasText(makerName)) {
            credit.append(" ").append(makerName.trim());
        }
        if (StringUtils.hasText(snsId)) {
            credit.append(" @").append(snsId.trim());
        }
        return credit.toString();
    }
}
