package com.mason.api.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 서비스 랜딩 페이지. 로그인 사용자 정보는 AuthModelAttributes가 공통으로 채워준다.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login-required")
    public String loginRequired() {
        return "login-required";
    }
}
