package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.ConsultarProfesionalUseCase;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarProfesionalUseCaseImpl implements ConsultarProfesionalUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public ConsultarProfesionalUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleProfesional ejecutar(String profesionalId) {
        Profesional p = profesionales.buscarPorId(profesionalId);
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        return new DetalleProfesional(p, profesionales.idsEspecialidades(profesionalId), profesionales.idsCentros(profesionalId));
    }
}
