package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.BuscarProfesionalPorUsuarioUseCase;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class BuscarProfesionalPorUsuarioUseCaseImpl implements BuscarProfesionalPorUsuarioUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public BuscarProfesionalPorUsuarioUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public Profesional ejecutar(String usuarioId) {
        return profesionales.buscarPorUsuarioId(usuarioId);
    }
}
