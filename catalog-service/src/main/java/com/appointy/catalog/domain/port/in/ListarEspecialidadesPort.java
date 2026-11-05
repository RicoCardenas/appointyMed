package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-08: especialidades disponibles; los administrativos pueden pedir tambien las inactivas. */
public interface ListarEspecialidadesPort {

    Especialidad[] ejecutar(boolean incluirInactivas);
}
