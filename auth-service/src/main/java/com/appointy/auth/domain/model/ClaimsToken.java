package com.appointy.auth.domain.model;

/** {@code expiraEn}: milisegundos desde 1970 en UTC. */
public record ClaimsToken(String usuarioId, Rol rol, String jti, long expiraEn) {}
