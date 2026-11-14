package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.Centro;

public interface CentroRepositoryGateway {

    Centro guardar(Centro centro);

    /** Devuelve null si no existe. */
    Centro buscarPorId(String id);

    /** Centros activos donde atiende el profesional. */
    Centro[] listarActivosDeProfesional(String profesionalId);

    /** Por nombre; con {@code incluirInactivos} false, solo los activos. */
    Centro[] listar(boolean incluirInactivos);
}
