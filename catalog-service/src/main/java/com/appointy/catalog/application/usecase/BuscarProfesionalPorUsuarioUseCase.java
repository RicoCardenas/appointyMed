package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.BuscarProfesionalPorUsuarioPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class BuscarProfesionalPorUsuarioUseCase implements BuscarProfesionalPorUsuarioPort {

    private final ProfesionalRepositoryPort profesionales;

    public BuscarProfesionalPorUsuarioUseCase(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public Profesional ejecutar(String usuarioId) {
        return profesionales.buscarPorUsuarioId(usuarioId);
    }
}
