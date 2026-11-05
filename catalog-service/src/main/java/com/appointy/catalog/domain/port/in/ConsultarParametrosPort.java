package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.ParametroServicio;

/** Parametros del servicio. */
public interface ConsultarParametrosPort {

    ParametroServicio[] ejecutar();
}
