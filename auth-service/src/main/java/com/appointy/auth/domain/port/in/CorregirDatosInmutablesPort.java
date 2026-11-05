package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;
import java.time.LocalDate;
import java.util.UUID;

/** CU-07: proceso administrativo. Un valor nulo significa "sin cambio". */
public interface CorregirDatosInmutablesPort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(
            UUID actorId,
            UUID pacienteUsuarioId,
            String cedula,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento) {}
}
