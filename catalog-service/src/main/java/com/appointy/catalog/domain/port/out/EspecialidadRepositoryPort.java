package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.Especialidad;

public interface EspecialidadRepositoryPort {

    Especialidad guardar(Especialidad especialidad);

    /** Devuelve null si no existe. */
    Especialidad buscarPorId(String id);

    /** Devuelve null si no existe. */
    Especialidad buscarPorNombre(String nombre);

    Especialidad[] listar(boolean incluirInactivas);
}
