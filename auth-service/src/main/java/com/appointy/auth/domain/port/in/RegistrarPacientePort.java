package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-01. {@code fechaNacimiento}: texto ISO yyyy-MM-dd. */
public interface RegistrarPacientePort {

    PacienteConCuenta ejecutar(Comando comando);

    record Comando(
            String correo,
            String clave,
            String cedula,
            String nombres,
            String apellidos,
            String fechaNacimiento,
            String telefono,
            String direccion,
            String ciudad) {}
}
