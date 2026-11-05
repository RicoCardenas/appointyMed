package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-18. */
public interface ReservarCitaPort {

    CitaConFranja ejecutar(Comando comando);

    record Comando(Actor actor, String especialidadId, String profesionalId, String centroId, String franjaId) {}
}
