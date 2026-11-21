package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.usecase.SesionUseCase;
import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiraEn, String usuarioId, Rol rol) {

    public static LoginResponse de(SesionUseCase.IniciarSesionResultado r) {
        return new LoginResponse(r.token(), "Bearer", Instant.ofEpochMilli(r.expiraEn()), r.usuarioId(), r.rol());
    }
}
