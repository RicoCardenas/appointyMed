package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Especialidad;

/** CU-11: crea una especialidad activa. */
public interface CrearEspecialidadPort {

    Especialidad ejecutar(Comando comando);

    record Comando(String nombre, String descripcion) {}
}
