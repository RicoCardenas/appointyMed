package com.appointy.notification.infrastructure.security;

/** Principal de Spring Security tras validar el JWT emitido por auth. {@code rol}: PACIENTE, PROFESIONAL o ADMINISTRATIVO. */
public record UsuarioAutenticado(String usuarioId, String rol) {}
