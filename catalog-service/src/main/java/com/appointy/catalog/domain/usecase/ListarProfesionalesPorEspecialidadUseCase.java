package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Profesional;

/** CU-09: profesionales activos de una especialidad activa. */
public interface ListarProfesionalesPorEspecialidadUseCase {

    Profesional[] ejecutar(String especialidadId);
}
