package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.rule.ReglasParametro;

final class Parametros {

    private Parametros() {}

    static int entero(ParametroRepositoryPort repositorio, String clave) {
        ParametroServicio p = repositorio.buscarPorClave(clave);
        if (p == null) {
            throw new IllegalStateException("Falta el parametro del servicio " + clave);
        }
        return ReglasParametro.comoEntero(p.valor());
    }
}
