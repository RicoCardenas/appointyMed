package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.usecase.ObtenerCitaServicioUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ObtenerCitaServicioUseCaseImpl implements ObtenerCitaServicioUseCase {

    private final CitaRepositoryGateway citas;

    public ObtenerCitaServicioUseCaseImpl(CitaRepositoryGateway citas) {
        this.citas = citas;
    }

    @Override
    @Transactional(readOnly = true)
    public Cita ejecutar(String citaId) {
        return AyudaCitas.buscarCita(citas, citaId);
    }
}
