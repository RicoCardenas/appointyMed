package com.appointy.notification;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationApplication {

    public static void main(String[] args) {
        // La base guarda instantes en TIMESTAMP sin zona y JDBC trabaja en UTC: la JVM en UTC evita desplazamientos segun la maquina.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(NotificationApplication.class, args);
    }
}
