package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.HistorialCita;

/** CU-24: el paciente y el profesional de la cita, y el personal administrativo. */
public interface ConsultarHistorialCitaPort {

    HistorialCita[] ejecutar(Actor actor, String citaId);
}
