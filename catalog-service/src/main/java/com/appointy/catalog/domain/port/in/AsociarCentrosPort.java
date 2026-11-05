package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.DetalleProfesional;

/** CU-12: reemplaza los centros del profesional; no se puede quitar un centro que tenga horarios o bloqueos. */
public interface AsociarCentrosPort {

    DetalleProfesional ejecutar(Comando comando);

    record Comando(String profesionalId, String[] centroIds) {}
}
