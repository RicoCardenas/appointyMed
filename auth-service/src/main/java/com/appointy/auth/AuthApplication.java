package com.appointy.auth;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        // La base guarda fechas y horas sin zona (TIME, TIMESTAMP) y JDBC trabaja en UTC. Si la JVM usara otra zona
        // (tu maquina en Bogota, AWS en UTC) una hora como 08:00 se guardaria desplazada. Fijar UTC lo evita.
        // La zona de la clinica para "hoy" es otra cosa: app.timezone.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(AuthApplication.class, args);
    }
}
