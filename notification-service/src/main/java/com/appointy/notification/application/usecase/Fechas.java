package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.exception.ValidacionException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/** Conversion de la fecha y hora locales de la clinica a milisegundos UTC (el dominio no usa java.time). */
final class Fechas {

    private Fechas() {}

    static long inicioEnMillis(String fecha, String hora, ZoneId zona) {
        try {
            return LocalDate.parse(fecha).atTime(LocalTime.parse(hora)).atZone(zona).toInstant().toEpochMilli();
        } catch (DateTimeException e) {
            // El formato ya paso la regla del dominio, pero la fecha puede no existir (2026-13-45, 2026-02-30).
            throw new ValidacionException("FECHA_INVALIDA", "La fecha u hora no existe");
        }
    }
}
