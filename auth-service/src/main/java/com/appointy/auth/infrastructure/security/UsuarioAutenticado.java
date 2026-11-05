package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.model.Rol;
import java.time.Instant;
import java.util.UUID;

/** Principal de Spring Security tras validar el JWT. */
public record UsuarioAutenticado(UUID usuarioId, Rol rol, String jti, Instant expiraEn) {}
