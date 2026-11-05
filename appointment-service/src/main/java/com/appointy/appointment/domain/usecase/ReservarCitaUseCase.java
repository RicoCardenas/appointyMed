package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-18. */
public interface ReservarCitaUseCase {

    CitaConFranja ejecutar(Comando comando);

    record Comando(Actor actor, String especialidadId, String profesionalId, String centroId, String franjaId) {}
}
