package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.model.Rol;

/** Principal de Spring Security tras validar el JWT. {@code expiraEn}: milisegundos desde 1970 en UTC. */
public record UsuarioAutenticado(String usuarioId, Rol rol, String jti, long expiraEn) {}
