package com.appointy.catalog.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, InternalProperties.class, AppointmentProperties.class})
public class InfraestructuraConfig {}
