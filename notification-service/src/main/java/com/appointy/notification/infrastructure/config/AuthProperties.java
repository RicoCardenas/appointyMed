package com.appointy.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}
