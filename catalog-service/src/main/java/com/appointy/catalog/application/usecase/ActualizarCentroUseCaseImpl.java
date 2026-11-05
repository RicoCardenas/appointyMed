package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.usecase.ActualizarCentroUseCase;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ActualizarCentroUseCaseImpl implements ActualizarCentroUseCase {

    private final CentroRepositoryGateway centros;

    public ActualizarCentroUseCaseImpl(CentroRepositoryGateway centros) {
        this.centros = centros;
    }

    @Override
    @Transactional
    public Centro ejecutar(Comando c) {
        Centro actual = centros.buscarPorId(c.id());
        if (actual == null) {
            throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
        }
        return centros.guardar(actual.actualizar(c.nombre().trim(), c.direccion().trim(), c.ciudad().trim(), c.telefono()));
    }
}
