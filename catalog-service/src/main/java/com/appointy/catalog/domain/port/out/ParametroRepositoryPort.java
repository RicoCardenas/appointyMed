package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.ParametroServicio;

public interface ParametroRepositoryPort {

    ParametroServicio[] listar();

    /** Devuelve null si no existe. */
    ParametroServicio buscarPorClave(String clave);

    ParametroServicio guardar(ParametroServicio parametro);
}
