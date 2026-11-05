package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.port.in.ActualizarCentroPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ActualizarCentroUseCase implements ActualizarCentroPort {

    private final CentroRepositoryPort centros;

    public ActualizarCentroUseCase(CentroRepositoryPort centros) {
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
