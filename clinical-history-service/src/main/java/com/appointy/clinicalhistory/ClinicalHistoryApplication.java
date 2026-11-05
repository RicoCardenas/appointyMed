package com.appointy.clinicalhistory;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClinicalHistoryApplication {

    public static void main(String[] args) {
        // La base guarda instantes en TIMESTAMP sin zona y JDBC trabaja en UTC: la JVM en UTC evita desplazamientos segun la maquina.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ClinicalHistoryApplication.class, args);
    }
}
