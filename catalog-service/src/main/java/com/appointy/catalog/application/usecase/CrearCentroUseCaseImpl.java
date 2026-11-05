package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.usecase.CrearCentroUseCase;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearCentroUseCaseImpl implements CrearCentroUseCase {

    private final CentroRepositoryGateway centros;

    public CrearCentroUseCaseImpl(CentroRepositoryGateway centros) {
        this.centros = centros;
    }

    @Override
    @Transactional
    public Centro ejecutar(Comando c) {
        return centros.guardar(new Centro(UUID.randomUUID().toString(), c.nombre().trim(), c.direccion().trim(), c.ciudad().trim(), c.telefono(), true));
    }
}
