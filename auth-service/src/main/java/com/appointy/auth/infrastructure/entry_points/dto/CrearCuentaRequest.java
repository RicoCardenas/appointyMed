package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearCuentaRequest(
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 8, max = 72) String clave,
        @NotNull Rol rol) {

    public CrearCuentaStaffPort.Comando aComando() {
        return new CrearCuentaStaffPort.Comando(correo, clave, rol);
    }
}
