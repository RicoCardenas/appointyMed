package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-05: correo, telefono, direccion y ciudad. Un valor nulo significa "sin cambio". */
public interface ActualizarDatosEditablesPort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(String usuarioId, String correo, String telefono, String direccion, String ciudad) {}
}
