package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.usecase.ConsultarEstadisticasCitasUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarEstadisticasCitasUseCaseImpl implements ConsultarEstadisticasCitasUseCase {

    private final CitaRepositoryGateway citas;

    public ConsultarEstadisticasCitasUseCaseImpl(CitaRepositoryGateway citas) {
        this.citas = citas;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasCitas ejecutar(Actor actor) {
        if (!actor.esAdministrativo()) {
            throw new AccesoProhibidoException("SOLO_ADMINISTRATIVO", "Solo el personal administrativo consulta las estadisticas");
        }
        return citas.estadisticas();
    }
}
