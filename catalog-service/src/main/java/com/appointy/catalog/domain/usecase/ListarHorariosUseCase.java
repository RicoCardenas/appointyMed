package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.HorarioAtencion;

/** Horarios de un profesional. */
public interface ListarHorariosUseCase {

    HorarioAtencion[] ejecutar(String profesionalId);
}
