package com.appointy.notification.domain.model;

/** Cualquier campo en null significa "sin filtrar por ese campo". */
public record FiltroNotificaciones(EstadoNotificacion estado, TipoNotificacion tipo, String citaId) {}
