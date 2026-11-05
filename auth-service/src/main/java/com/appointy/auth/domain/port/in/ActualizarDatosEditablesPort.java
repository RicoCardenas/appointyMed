package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;
import java.util.UUID;

/** CU-05: correo, telefono, direccion y ciudad. Un valor nulo significa "sin cambio". */
public interface ActualizarDatosEditablesPort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(UUID usuarioId, String correo, String telefono, String direccion, String ciudad) {}
}
