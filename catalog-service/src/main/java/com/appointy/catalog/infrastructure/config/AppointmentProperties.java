package com.appointy.catalog.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.appointment")
public record AppointmentProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}
