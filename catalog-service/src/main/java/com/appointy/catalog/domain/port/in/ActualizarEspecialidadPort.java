package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-11: edita nombre y descripcion. */
public interface ActualizarEspecialidadPort {

    Especialidad ejecutar(Comando comando);

    record Comando(String id, String nombre, String descripcion) {}
}
