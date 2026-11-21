package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.rule.FechaIso;

/** Auxiliar de validacion de fechas ISO. */
final class Fechas {

    private Fechas() {}

    /** Valida el formato y que no sea futura (compara texto ISO); devuelve la fecha. */
    static String nacimientoValido(String iso, String hoy) {
        if (!FechaIso.esValida(iso)) {
            throw new ValidacionException("FECHA_INVALIDA", "La fecha debe tener el formato yyyy-MM-dd");
        }
        if (iso.compareTo(hoy) > 0) {
            throw new ValidacionException("FECHA_NACIMIENTO_INVALIDA", "La fecha de nacimiento no puede ser futura");
        }
        return iso;
    }
}
