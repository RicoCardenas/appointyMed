package com.appointy.appointment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Convierte el reloj de la clinica en los valores simples que usa el dominio: instantes en milisegundos, fechas ISO
 * y "minutos locales" (minutos desde 1970 en hora de la clinica) para comparar el inicio de una franja con ahora.
 */
public class RelojClinica {

    public static final String FECHA_MAXIMA = "9999-12-31";
    public static final String FECHA_MINIMA = "0001-01-01";

    private final Clock reloj;

    public RelojClinica(Clock reloj) {
        this.reloj = reloj;
    }

    public long ahoraMillis() {
        return reloj.millis();
    }

    public String hoy() {
        return LocalDate.now(reloj).toString();
    }

    public long ahoraMinutosLocales() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return minutosLocales(ahora.toLocalDate().toString(), ahora.getHour() * 60 + ahora.getMinute());
    }

    public static long minutosLocales(String fecha, int minutosDelDia) {
        return LocalDate.parse(fecha).toEpochDay() * 1440 + minutosDelDia;
    }

    public String sumarDias(String fecha, int dias) {
        return LocalDate.parse(fecha).plusDays(dias).toString();
    }
}
