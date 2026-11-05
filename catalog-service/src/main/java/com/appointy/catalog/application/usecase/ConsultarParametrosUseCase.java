package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.port.in.ConsultarParametrosPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarParametrosUseCase implements ConsultarParametrosPort {

    private final ParametroRepositoryPort parametros;

    public ConsultarParametrosUseCase(ParametroRepositoryPort parametros) {
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public ParametroServicio[] ejecutar() {
        return parametros.listar();
    }
}
