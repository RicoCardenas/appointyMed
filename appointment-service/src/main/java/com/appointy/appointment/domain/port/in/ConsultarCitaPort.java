package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

public interface ConsultarCitaPort {

    CitaConFranja ejecutar(Actor actor, String citaId);
}
