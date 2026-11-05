package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Profesional;

/** CU-11: crea el perfil de un profesional; usuarioId es la cuenta creada antes en auth. */
public interface CrearProfesionalUseCase {

    Profesional ejecutar(Comando comando);

    record Comando(String usuarioId, String nombres, String apellidos, String registroMedico) {}
}
