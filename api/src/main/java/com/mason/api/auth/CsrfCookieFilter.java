package com.mason.api.auth;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CsrfToken은 지연 로딩되어 누군가 getToken()을 호출하기 전까지는
 * CookieCsrfTokenRepository가 쿠키를 내려보내지 않는다. 서버 렌더링 폼이 없는
 * SPA 클라이언트를 위해 매 요청마다 강제로 로드시켜 XSRF-TOKEN 쿠키가 항상
 * 내려가도록 한다.
 */
public final class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        csrfToken.getToken();
        filterChain.doFilter(request, response);
    }
}