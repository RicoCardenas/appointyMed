package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.DetalleProfesional;

/** Detalle de un profesional con sus especialidades y centros. */
public interface ConsultarProfesionalUseCase {

    DetalleProfesional ejecutar(String profesionalId);
}
