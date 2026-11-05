package com.appointy.auth.domain.model;

/** {@code expiraEn}: milisegundos desde 1970 en UTC. */
public record TokenEmitido(String token, String jti, long expiraEn) {}
