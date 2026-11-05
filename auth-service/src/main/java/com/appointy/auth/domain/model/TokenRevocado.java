package com.appointy.auth.domain.model;

/** {@code expiraEn}: milisegundos desde 1970 en UTC. */
public record TokenRevocado(String jti, long expiraEn) {}
