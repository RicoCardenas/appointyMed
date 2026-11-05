package com.appointy.clinicalhistory.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, InternalProperties.class, CatalogProperties.class, AppointmentProperties.class})
public class InfraestructuraConfig {}
