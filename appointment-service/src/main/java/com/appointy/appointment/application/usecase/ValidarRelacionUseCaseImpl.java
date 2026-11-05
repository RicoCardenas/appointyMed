package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.usecase.ValidarRelacionUseCase;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ValidarRelacionUseCaseImpl implements ValidarRelacionUseCase {

    private final CitaRepositoryGateway citas;

    public ValidarRelacionUseCaseImpl(CitaRepositoryGateway citas) {
        this.citas = citas;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ejecutar(String profesionalId, String pacienteId) {
        return citas.existeRelacion(profesionalId, pacienteId);
    }
}
