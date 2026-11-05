package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.ValidarAsociacionUseCase;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ValidarAsociacionUseCaseImpl implements ValidarAsociacionUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public ValidarAsociacionUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public Resultado ejecutar(String profesionalId, String especialidadId, String centroId) {
        Profesional p = profesionales.buscarPorId(profesionalId);
        if (p == null) {
            return new Resultado(false, false);
        }
        boolean asociado = profesionales.tieneEspecialidad(profesionalId, especialidadId) && profesionales.atiendeEnCentro(profesionalId, centroId);
        return new Resultado(p.activo(), asociado);
    }
}
