package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import java.time.Instant;
import java.util.UUID;

public record CuentaResponse(UUID usuarioId, String correo, Rol rol, boolean activo, Instant creadoEn) {

    public static CuentaResponse de(Usuario u) {
        return new CuentaResponse(u.id(), u.correo(), u.rol(), u.activo(), u.creadoEn());
    }
}
