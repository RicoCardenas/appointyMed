package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.usecase.DesactivarCentroUseCase;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class DesactivarCentroUseCaseImpl implements DesactivarCentroUseCase {

    private final CentroRepositoryGateway centros;

    public DesactivarCentroUseCaseImpl(CentroRepositoryGateway centros) {
        this.centros = centros;
    }

    @Override
    @Transactional
    public void ejecutar(String id) {
        Centro actual = centros.buscarPorId(id);
        if (actual == null) {
            throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
        }
        if (actual.activo()) {
            centros.guardar(actual.desactivar());
        }
    }
}
