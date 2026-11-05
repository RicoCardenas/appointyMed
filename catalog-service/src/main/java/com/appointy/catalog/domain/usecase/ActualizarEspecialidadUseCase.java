package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-11: edita nombre y descripcion. */
public interface ActualizarEspecialidadUseCase {

    Especialidad ejecutar(Comando comando);

    record Comando(String id, String nombre, String descripcion) {}
}
