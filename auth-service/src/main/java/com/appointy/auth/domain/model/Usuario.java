package com.appointy.auth.domain.model;

/**
 * Cuenta de acceso. Los instantes son milisegundos desde 1970 en UTC y {@code id} es el texto de un UUID.
 * {@code version} es nula mientras la cuenta no se ha guardado.
 */
public record Usuario(
        String id,
        String correo,
        String claveHash,
        Rol rol,
        boolean activo,
        long creadoEn,
        long actualizadoEn,
        Long version) {

    public static Usuario nuevo(String id, String correo, String claveHash, Rol rol, long ahora) {
        return new Usuario(id, correo, claveHash, rol, true, ahora, ahora, null);
    }

    public Usuario conCorreo(String nuevoCorreo, long ahora) {
        return new Usuario(id, nuevoCorreo, claveHash, rol, activo, creadoEn, ahora, version);
    }
}
