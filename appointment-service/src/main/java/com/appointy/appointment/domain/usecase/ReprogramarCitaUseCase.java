package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-19. */
public interface ReprogramarCitaUseCase {

    CitaConFranja ejecutar(Comando comando);

    record Comando(Actor actor, String citaId, String nuevaFranjaId) {}
}
