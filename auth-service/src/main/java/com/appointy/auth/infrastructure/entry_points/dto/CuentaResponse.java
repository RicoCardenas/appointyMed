package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import java.time.Instant;

public record CuentaResponse(String usuarioId, String correo, Rol rol, boolean activo, Instant creadoEn) {

    public static CuentaResponse de(Usuario u) {
        return new CuentaResponse(u.id(), u.correo(), u.rol(), u.activo(), Instant.ofEpochMilli(u.creadoEn()));
    }
}
