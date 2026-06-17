package com.mason.api.support;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 공통 헤더(fragments/layout :: header)가 참조하는 username/currentDiscordIcon/currentDiscordId를
 * 뷰를 렌더링하는 모든 컨트롤러에 자동으로 채워준다.
 */
@ControllerAdvice
public class AuthModelAttributes {

    @ModelAttribute
    public void addUserAttributes(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getAttribute("guildNickname"));
            model.addAttribute("currentDiscordIcon", principal.getAttribute("avatar"));
            model.addAttribute("currentDiscordId", principal.getAttribute("id"));
        }
    }
}