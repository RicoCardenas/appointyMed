package com.appointy.catalog.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.internal")
public record InternalProperties(String apiKey) {}
