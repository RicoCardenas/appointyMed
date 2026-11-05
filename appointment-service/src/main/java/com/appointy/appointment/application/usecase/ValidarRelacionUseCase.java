package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.port.in.ValidarRelacionPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ValidarRelacionUseCase implements ValidarRelacionPort {

    private final CitaRepositoryPort citas;

    public ValidarRelacionUseCase(CitaRepositoryPort citas) {
        this.citas = citas;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ejecutar(String profesionalId, String pacienteId) {
        return citas.existeRelacion(profesionalId, pacienteId);
    }
}
