package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Centro;

/** CU-11: edita un centro. */
public interface ActualizarCentroPort {

    Centro ejecutar(Comando comando);

    record Comando(String id, String nombre, String direccion, String ciudad, String telefono) {}
}
