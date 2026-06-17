package com.mason.api.auth;

import java.util.function.Supplier;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 기본 XorCsrfTokenRequestAttributeHandler는 BREACH 방어를 위해 노출되는 토큰을
 * XOR로 마스킹하지만, CookieCsrfTokenRepository는 쿠키에 원본 토큰을 저장한다.
 * SPA가 쿠키 값을 그대로 읽어 헤더로 보내는 패턴을 지원하려면, 헤더로 들어온 값은
 * 마스킹 해제 없이 그대로 비교해야 한다 (Spring Security 공식 문서 권장 패턴).
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return StringUtils.hasText(headerValue) ? headerValue : this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}