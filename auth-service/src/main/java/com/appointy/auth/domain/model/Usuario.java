package com.appointy.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Cuenta de acceso. {@code version} es nula mientras la cuenta no se ha guardado. */
public record Usuario(
        UUID id,
        String correo,
        String claveHash,
        Rol rol,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn,
        Long version) {

    public static Usuario nuevo(UUID id, String correo, String claveHash, Rol rol, Instant ahora) {
        return new Usuario(id, correo, claveHash, rol, true, ahora, ahora, null);
    }

    public Usuario conCorreo(String nuevoCorreo, Instant ahora) {
        return new Usuario(id, nuevoCorreo, claveHash, rol, activo, creadoEn, ahora, version);
    }
}
