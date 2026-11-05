package com.appointy.appointment.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}
