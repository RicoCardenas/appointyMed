package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.Especialidad;

public interface EspecialidadRepositoryGateway {

    Especialidad guardar(Especialidad especialidad);

    /** Devuelve null si no existe. */
    Especialidad buscarPorId(String id);

    /** Devuelve null si no existe. */
    Especialidad buscarPorNombre(String nombre);

    Especialidad[] listar(boolean incluirInactivas);
}
