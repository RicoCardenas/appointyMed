package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.usecase.ListarCentrosUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ListarCentrosUseCaseImpl implements ListarCentrosUseCase {

    private final CentroRepositoryGateway centros;

    public ListarCentrosUseCaseImpl(CentroRepositoryGateway centros) {
        this.centros = centros;
    }

    @Override
    @Transactional(readOnly = true)
    public Centro[] ejecutar(boolean incluirInactivos) {
        return centros.listar(incluirInactivos);
    }
}
