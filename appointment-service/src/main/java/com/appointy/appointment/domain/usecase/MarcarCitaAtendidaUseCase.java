package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;

/** CU-23: el profesional de la cita la marca COMPLETADA o NO_ASISTIDA. */
public interface MarcarCitaAtendidaUseCase {

    CitaConFranja ejecutar(Actor actor, String citaId, EstadoCita nuevoEstado);
}
