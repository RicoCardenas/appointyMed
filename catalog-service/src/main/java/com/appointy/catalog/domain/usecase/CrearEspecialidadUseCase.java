package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-11: crea una especialidad activa. */
public interface CrearEspecialidadUseCase {

    Especialidad ejecutar(Comando comando);

    record Comando(String nombre, String descripcion) {}
}
