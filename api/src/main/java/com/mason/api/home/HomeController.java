package com.mason.api.home;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getAttribute("guildNickname"));
            model.addAttribute("avatar", principal.getAttribute("avatar"));
            model.addAttribute("discordId", principal.getAttribute("id"));
        }
        return "index";
    }
}
