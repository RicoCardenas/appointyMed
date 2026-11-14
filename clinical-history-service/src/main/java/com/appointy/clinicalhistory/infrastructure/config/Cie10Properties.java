package com.appointy.clinicalhistory.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cie10")
public record Cie10Properties(String baseUrl, int connectTimeoutMs, int readTimeoutMs, int ttlHoras) {}
