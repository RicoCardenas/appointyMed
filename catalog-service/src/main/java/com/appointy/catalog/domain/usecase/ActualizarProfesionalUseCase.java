package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Profesional;

/** CU-11: edita nombres, apellidos y registro medico (usuarioId no cambia). */
public interface ActualizarProfesionalUseCase {

    Profesional ejecutar(Comando comando);

    record Comando(String id, String nombres, String apellidos, String registroMedico) {}
}
