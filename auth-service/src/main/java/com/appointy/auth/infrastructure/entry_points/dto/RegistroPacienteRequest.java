package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.port.in.RegistrarPacientePort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegistroPacienteRequest(
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 8, max = 72) String clave,
        @NotBlank @Pattern(regexp = "^[0-9A-Za-z-]{5,20}$", message = "debe tener de 5 a 20 caracteres alfanumericos") String cedula,
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotNull @Past LocalDate fechaNacimiento,
        @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "telefono invalido") String telefono,
        @Size(max = 200) String direccion,
        @Size(max = 100) String ciudad) {

    public RegistrarPacientePort.Comando aComando() {
        return new RegistrarPacientePort.Comando(
                correo, clave, cedula, nombres, apellidos, fechaNacimiento.toString(), telefono, direccion, ciudad);
    }
}
