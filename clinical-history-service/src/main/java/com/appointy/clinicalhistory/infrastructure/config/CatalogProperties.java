package com.appointy.clinicalhistory.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.catalog")
public record CatalogProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}
