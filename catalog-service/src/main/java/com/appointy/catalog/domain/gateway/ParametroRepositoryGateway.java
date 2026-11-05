package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.ParametroServicio;

public interface ParametroRepositoryGateway {

    ParametroServicio[] listar();

    /** Devuelve null si no existe. */
    ParametroServicio buscarPorClave(String clave);

    ParametroServicio guardar(ParametroServicio parametro);
}
