package com.appointy.notification.domain.model;

/**
 * Hecho que reporta appointment (CU-33). {@code tipo}: CONFIRMADA, REPROGRAMADA o CANCELADA. {@code fecha}: yyyy-MM-dd local
 * de la clinica; {@code horaInicio}: HH:mm. Solo ids, sin datos personales.
 */
public record SolicitudCita(
        String tipo, String citaId, String pacienteId, String profesionalId, String centroId, String fecha, String horaInicio, String actorId) {}
