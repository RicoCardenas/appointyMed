package com.appointy.catalog.domain.rule;

import com.appointy.catalog.domain.exception.ValidacionException;

public final class ReglasBloqueo {

    private ReglasBloqueo() {}

    /** Las fechas son texto ISO yyyy-MM-dd, que ordena igual como texto que como fecha. */
    public static void validar(String fechaInicio, String fechaFin, String motivo) {
        if (fechaInicio == null || fechaFin == null || fechaFin.compareTo(fechaInicio) < 0) {
            throw new ValidacionException("RANGO_INVALIDO", "La fecha de fin no puede ser anterior a la de inicio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ValidacionException("MOTIVO_REQUERIDO", "El motivo del bloqueo es obligatorio");
        }
    }
}
