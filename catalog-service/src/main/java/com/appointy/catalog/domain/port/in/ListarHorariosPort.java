package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.HorarioAtencion;

/** Horarios de un profesional. */
public interface ListarHorariosPort {

    HorarioAtencion[] ejecutar(String profesionalId);
}
