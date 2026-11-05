package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.DesactivarProfesionalPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class DesactivarProfesionalUseCase implements DesactivarProfesionalPort {

    private final ProfesionalRepositoryPort profesionales;

    public DesactivarProfesionalUseCase(ProfesionalRepositoryPort profesionales) {
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
