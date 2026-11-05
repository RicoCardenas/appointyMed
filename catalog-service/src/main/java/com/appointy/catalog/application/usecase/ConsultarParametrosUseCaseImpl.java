package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.usecase.ConsultarParametrosUseCase;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarParametrosUseCaseImpl implements ConsultarParametrosUseCase {

    private final ParametroRepositoryGateway parametros;

    public ConsultarParametrosUseCaseImpl(ParametroRepositoryGateway parametros) {
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public ParametroServicio[] ejecutar() {
        return parametros.listar();
    }
}
