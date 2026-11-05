package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Centro;

/** CU-11: crea un centro de atencion activo. */
public interface CrearCentroPort {

    Centro ejecutar(Comando comando);

    record Comando(String nombre, String direccion, String ciudad, String telefono) {}
}
