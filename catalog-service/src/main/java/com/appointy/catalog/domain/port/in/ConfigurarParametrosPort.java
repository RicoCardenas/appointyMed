package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.ParametroServicio;

/** CU-15: actualiza uno o varios parametros; solo cuentan clave y valor. */
public interface ConfigurarParametrosPort {

    ParametroServicio[] ejecutar(Comando comando);

    record Comando(ParametroServicio[] parametros) {}
}
