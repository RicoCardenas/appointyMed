package com.appointy.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** API HTTP de Brevo (puerto 443). Sirve donde el SMTP esta bloqueado, por ejemplo Render gratuito. */
@ConfigurationProperties(prefix = "app.mail.brevo")
public record BrevoProperties(String baseUrl, String apiKey, int connectTimeoutMs, int readTimeoutMs) {}
