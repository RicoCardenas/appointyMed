package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Profesional;

/** CU-11: edita nombres, apellidos y registro medico (usuarioId no cambia). */
public interface ActualizarProfesionalPort {

    Profesional ejecutar(Comando comando);

    record Comando(String id, String nombres, String apellidos, String registroMedico) {}
}
