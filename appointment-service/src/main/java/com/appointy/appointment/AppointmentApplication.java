package com.appointy.appointment;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppointmentApplication {

    public static void main(String[] args) {
        // La base guarda horas sin zona (TIME, TIMESTAMP) y JDBC trabaja en UTC. Fijar UTC en la JVM evita que una hora como
        // 08:00 se guarde desplazada segun la maquina. La zona de la clinica para "hoy" es otra cosa: app.timezone.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(AppointmentApplication.class, args);
    }
}
