package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.port.in.IniciarSesionPort;
import java.time.Instant;
import java.util.UUID;

public record LoginResponse(String accessToken, String tokenType, Instant expiraEn, UUID usuarioId, Rol rol) {

    public static LoginResponse de(IniciarSesionPort.Resultado r) {
        return new LoginResponse(r.token(), "Bearer", r.expiraEn(), r.usuarioId(), r.rol());
    }
}
