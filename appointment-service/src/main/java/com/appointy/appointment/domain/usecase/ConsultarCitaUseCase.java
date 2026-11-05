package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

public interface ConsultarCitaUseCase {

    CitaConFranja ejecutar(Actor actor, String citaId);
}
