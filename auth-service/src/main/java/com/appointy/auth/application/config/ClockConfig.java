package com.appointy.auth.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Todo el servicio trabaja en UTC; las pruebas inyectan un reloj fijo. */
    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
