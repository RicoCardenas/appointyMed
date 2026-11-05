package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Bloqueo;

/** CU-16: registra un bloqueo si appointment confirma que no hay citas CONFIRMADAS en el intervalo. */
public interface BloquearFranjasUseCase {

    Bloqueo ejecutar(Comando comando);

    record Comando(String actorId, String profesionalId, String centroId, String fechaInicio, String fechaFin, String motivo) {}
}
