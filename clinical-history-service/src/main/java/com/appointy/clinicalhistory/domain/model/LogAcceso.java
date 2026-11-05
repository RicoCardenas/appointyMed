package com.appointy.clinicalhistory.domain.model;

/** RF-30: quien, cuando y que paciente. {@code pacienteId} y {@code entradaId} son null si aun no se conocian al rechazar. */
public record LogAcceso(
        String id,
        String usuarioId,
        String rol,
        String pacienteId,
        AccionAcceso accion,
        String entradaId,
        long fecha,
        ResultadoAcceso resultado) {}
