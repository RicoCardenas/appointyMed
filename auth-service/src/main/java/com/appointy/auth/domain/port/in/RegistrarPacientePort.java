package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;
import java.time.LocalDate;

/** CU-01. */
public interface RegistrarPacientePort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(
            String correo,
            String clave,
            String cedula,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            String telefono,
            String direccion,
            String ciudad) {}
}
