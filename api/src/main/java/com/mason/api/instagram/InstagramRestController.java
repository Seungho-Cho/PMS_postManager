package com.mason.api.instagram;

import com.mason.api.instagram.dto.InstagramStatusResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instagram")
public class InstagramRestController {

    private final InstagramKeyService instagramKeyService;

    public InstagramRestController(InstagramKeyService instagramKeyService) {
        this.instagramKeyService = instagramKeyService;
    }

    @GetMapping("/status")
    public InstagramStatusResponse status() {
        return instagramKeyService.checkStatus();
    }

    @PostMapping("/refresh")
    public InstagramStatusResponse refresh() {
        return instagramKeyService.refresh();
    }
}