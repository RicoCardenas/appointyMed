package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.Centro;

public interface CentroRepositoryPort {

    Centro guardar(Centro centro);

    /** Devuelve null si no existe. */
    Centro buscarPorId(String id);

    /** Centros activos donde atiende el profesional. */
    Centro[] listarActivosDeProfesional(String profesionalId);
}
