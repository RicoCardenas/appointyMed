package com.appointy.notification.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, InternalProperties.class, AuthProperties.class, BrevoProperties.class})
public class InfraestructuraConfig {}
