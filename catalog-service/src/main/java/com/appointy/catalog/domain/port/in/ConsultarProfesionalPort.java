package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.DetalleProfesional;

/** Detalle de un profesional con sus especialidades y centros. */
public interface ConsultarProfesionalPort {

    DetalleProfesional ejecutar(String profesionalId);
}
