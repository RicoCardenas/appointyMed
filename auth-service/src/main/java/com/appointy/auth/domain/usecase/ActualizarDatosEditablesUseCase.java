package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-05: correo, telefono, direccion y ciudad. Un valor nulo significa "sin cambio". */
public interface ActualizarDatosEditablesUseCase {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(String usuarioId, String correo, String telefono, String direccion, String ciudad) {}
}
