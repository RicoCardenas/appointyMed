package com.appointy.catalog.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.festivos")
public record FestivosProperties(String baseUrl, String pais, int connectTimeoutMs, int readTimeoutMs, int ttlHoras) {}
