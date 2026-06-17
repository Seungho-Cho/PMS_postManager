package com.mason.api.config;

import java.util.List;

import com.mason.api.config.dto.AppConfigResponse;
import com.mason.api.config.dto.CreateAppConfigRequest;
import com.mason.api.config.dto.UpdateAppConfigRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * R2/Discord/Instagram/X/Groq 등 외부 연동 키-값 설정 관리 API.
 * 어드민 권한 체크는 아직 없고, 로그인한 사용자면 누구나 접근 가능하다 (차후 구현).
 */
@RestController
@RequestMapping("/api/configs")
public class AppConfigRestController {

    private final AppConfigService appConfigService;

    public AppConfigRestController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping
    public List<AppConfigResponse> findAll() {
        return appConfigService.findAll().stream()
            .map(AppConfigResponse::from)
            .toList();
    }

    @GetMapping("/{key}")
    public AppConfigResponse findByKey(@PathVariable String key) {
        return AppConfigResponse.from(appConfigService.findByKey(key));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody CreateAppConfigRequest request) {
        appConfigService.create(request.key(), request.value());
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> update(
        @PathVariable String key,
        @Valid @RequestBody UpdateAppConfigRequest request
    ) {
        appConfigService.updateValue(key, request.value());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        appConfigService.delete(key);
        return ResponseEntity.noContent().build();
    }
}