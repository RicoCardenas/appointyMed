package com.appointy.appointment.application.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /**
     * El reloj usa la zona horaria de la clinica (app.timezone): las franjas guardan fecha y hora locales de la clinica
     * y "hoy" sale de aqui. Los instantes ({@code millis()}) no dependen de la zona y se guardan en UTC.
     */
    @Bean
    Clock reloj(@Value("${app.timezone}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
