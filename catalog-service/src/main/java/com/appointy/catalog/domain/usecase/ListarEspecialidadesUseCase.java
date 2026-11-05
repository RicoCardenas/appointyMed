package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-08: especialidades disponibles; los administrativos pueden pedir tambien las inactivas. */
public interface ListarEspecialidadesUseCase {

    Especialidad[] ejecutar(boolean incluirInactivas);
}
