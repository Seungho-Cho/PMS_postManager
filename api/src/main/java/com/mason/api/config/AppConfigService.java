package com.mason.api.config;

import java.util.List;

import com.mason.api.config.entity.AppConfig;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

/**
 * 외부 연동 키-값 설정({@link AppConfig})에 대한 조회/생성/수정/삭제를 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigService(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    public List<AppConfig> findAll() {
        return appConfigRepository.findAll();
    }

    public AppConfig findByKey(String key) {
        return appConfigRepository.findById(key)
            .orElseThrow(() -> new EntityNotFoundException(key));
    }

    @Transactional
    public void create(String key, String value) {
        if (appConfigRepository.existsById(key)) {
            throw new EntityExistsException(key);
        }
        appConfigRepository.save(new AppConfig(key, value));
    }

    @Transactional
    public void updateValue(String key, String value) {
        findByKey(key).updateValue(value);
    }

    @Transactional
    public void delete(String key) {
        appConfigRepository.deleteById(key);
    }
}
