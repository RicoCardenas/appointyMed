package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-20. {@code motivo} puede ser null. */
public interface CancelarCitaPort {

    CitaConFranja ejecutar(Comando comando);

    record Comando(Actor actor, String citaId, String motivo) {}
}
