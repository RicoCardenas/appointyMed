package com.appointy.clinicalhistory.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code relacionesTtlSegundos}: vigencia en cache de una relacion profesional-paciente ya confirmada. */
@ConfigurationProperties(prefix = "app.appointment")
public record AppointmentProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs, int relacionesTtlSegundos) {}
