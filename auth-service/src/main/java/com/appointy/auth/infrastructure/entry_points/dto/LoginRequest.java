package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.port.in.IniciarSesionPort;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 150) String correo, @NotBlank @Size(max = 72) String clave) {

    public IniciarSesionPort.Comando aComando() {
        return new IniciarSesionPort.Comando(correo, clave);
    }
}
