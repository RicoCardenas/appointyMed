package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-19. */
public interface ReprogramarCitaPort {

    CitaConFranja ejecutar(Comando comando);

    record Comando(Actor actor, String citaId, String nuevaFranjaId) {}
}
