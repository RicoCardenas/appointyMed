package com.appointy.clinicalhistory.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Solo se usan instantes (millis, UTC); clinical no necesita la zona de la clinica. */
    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
