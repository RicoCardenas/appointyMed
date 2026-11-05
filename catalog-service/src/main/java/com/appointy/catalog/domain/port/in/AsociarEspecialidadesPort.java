package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.DetalleProfesional;

/** CU-12: reemplaza las especialidades del profesional por las indicadas (todas deben existir y estar activas). */
public interface AsociarEspecialidadesPort {

    DetalleProfesional ejecutar(Comando comando);

    record Comando(String profesionalId, String[] especialidadIds) {}
}
