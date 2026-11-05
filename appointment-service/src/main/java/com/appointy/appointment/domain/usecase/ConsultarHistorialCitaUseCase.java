package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.HistorialCita;

/** CU-24: el paciente y el profesional de la cita, y el personal administrativo. */
public interface ConsultarHistorialCitaUseCase {

    HistorialCita[] ejecutar(Actor actor, String citaId);
}
