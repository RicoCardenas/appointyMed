package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Centro;

/** CU-11: edita un centro. */
public interface ActualizarCentroUseCase {

    Centro ejecutar(Comando comando);

    record Comando(String id, String nombre, String direccion, String ciudad, String telefono) {}
}
