package com.appointy.notification.application.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    /** Zona de la clinica: la fecha y hora de la cita llegan en hora local y el recordatorio se calcula sobre ellas. */
    @Bean
    ZoneId zonaClinica(@Value("${app.timezone:America/Bogota}") String zona) {
        return ZoneId.of(zona);
    }
}
