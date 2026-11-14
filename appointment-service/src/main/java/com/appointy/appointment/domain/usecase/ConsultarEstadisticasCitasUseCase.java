package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.EstadisticasCitas;

/** Conteo de citas por estado. Solo ADMINISTRATIVO. */
public interface ConsultarEstadisticasCitasUseCase {

    EstadisticasCitas ejecutar(Actor actor);
}
