package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.ParametroServicio;

/** Parametros del servicio. */
public interface ConsultarParametrosUseCase {

    ParametroServicio[] ejecutar();
}
