package com.mason.api.config;

import com.mason.api.config.entity.AppConfig;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}