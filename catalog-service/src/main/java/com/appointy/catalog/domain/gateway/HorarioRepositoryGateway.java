package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.HorarioAtencion;

public interface HorarioRepositoryGateway {

    HorarioAtencion guardar(HorarioAtencion horario);

    /** Devuelve null si no existe. */
    HorarioAtencion buscarPorId(String id);

    HorarioAtencion[] listarDeProfesional(String profesionalId);

    int contarPorProfesionalYCentro(String profesionalId, String centroId);
}
