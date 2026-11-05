package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.port.in.DesactivarCentroPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class DesactivarCentroUseCase implements DesactivarCentroPort {

    private final CentroRepositoryPort centros;

    public DesactivarCentroUseCase(CentroRepositoryPort centros) {
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
