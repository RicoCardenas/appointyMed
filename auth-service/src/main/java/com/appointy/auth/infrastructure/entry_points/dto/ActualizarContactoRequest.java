package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.port.in.ActualizarDatosEditablesPort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Solo datos editables (RF-05). Cualquier otro campo, como la cedula, se rechaza con 400. */
public record ActualizarContactoRequest(
        @Email @Size(max = 150) String correo,
        @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "telefono invalido") String telefono,
        @Size(min = 1, max = 200) String direccion,
        @Size(min = 1, max = 100) String ciudad) {

    public ActualizarDatosEditablesPort.Comando aComando(UUID usuarioId) {
        return new ActualizarDatosEditablesPort.Comando(usuarioId, correo, telefono, direccion, ciudad);
    }
}
