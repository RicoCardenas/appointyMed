package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.HorarioAtencion;

public interface HorarioRepositoryPort {

    HorarioAtencion guardar(HorarioAtencion horario);

    /** Devuelve null si no existe. */
    HorarioAtencion buscarPorId(String id);

    HorarioAtencion[] listarDeProfesional(String profesionalId);

    int contarPorProfesionalYCentro(String profesionalId, String centroId);
}
