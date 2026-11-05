package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Profesional;

/** CU-09: profesionales activos de una especialidad activa. */
public interface ListarProfesionalesPorEspecialidadPort {

    Profesional[] ejecutar(String especialidadId);
}
