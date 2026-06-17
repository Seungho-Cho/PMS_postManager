package com.mason.api.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppConfigViewController {

    @GetMapping("/admin/configs")
    public String configs() {
        return "admin/configs";
    }
}