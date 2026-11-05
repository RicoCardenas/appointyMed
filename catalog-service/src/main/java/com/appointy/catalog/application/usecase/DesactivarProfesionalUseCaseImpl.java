package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.DesactivarProfesionalUseCase;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class DesactivarProfesionalUseCaseImpl implements DesactivarProfesionalUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public DesactivarProfesionalUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional
    public void ejecutar(String id) {
        Profesional actual = profesionales.buscarPorId(id);
        if (actual == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        if (actual.activo()) {
            profesionales.guardar(actual.desactivar());
        }
    }
}
