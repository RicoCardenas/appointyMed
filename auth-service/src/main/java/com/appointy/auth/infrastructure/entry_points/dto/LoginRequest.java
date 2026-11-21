package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.usecase.SesionUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 150) String correo, @NotBlank @Size(max = 72) String clave) {

    public SesionUseCase.IniciarSesionComando aComando() {
        return new SesionUseCase.IniciarSesionComando(correo, clave);
    }
}
