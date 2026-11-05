package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.ValidacionException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** El dominio guarda las fechas como texto ISO; aqui, fuera del dominio, se interpretan. */
final class Fechas {

    private Fechas() {}

    static LocalDate parsear(String iso) {
        try {
            return LocalDate.parse(iso);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ValidacionException("FECHA_INVALIDA", "La fecha debe tener el formato yyyy-MM-dd");
        }
    }

    /** Valida el formato y que no sea futura; devuelve el texto ISO normalizado. */
    static String nacimientoValido(String iso, LocalDate hoy) {
        LocalDate fecha = parsear(iso);
        if (fecha.isAfter(hoy)) {
            throw new ValidacionException("FECHA_NACIMIENTO_INVALIDA", "La fecha de nacimiento no puede ser futura");
        }
        return fecha.toString();
    }
}
