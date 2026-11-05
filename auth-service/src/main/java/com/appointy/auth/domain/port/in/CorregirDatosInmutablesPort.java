package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-07: proceso administrativo. Un valor nulo significa "sin cambio". {@code fechaNacimiento}: texto ISO. */
public interface CorregirDatosInmutablesPort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(
            String actorId,
            String pacienteUsuarioId,
            String cedula,
            String nombres,
            String apellidos,
            String fechaNacimiento) {}
}
