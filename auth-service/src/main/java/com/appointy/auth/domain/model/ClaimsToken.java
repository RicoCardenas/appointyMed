package com.appointy.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ClaimsToken(UUID usuarioId, Rol rol, String jti, Instant expiraEn) {}
