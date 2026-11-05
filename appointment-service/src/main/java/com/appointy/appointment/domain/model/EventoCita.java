package com.appointy.appointment.domain.model;

/**
 * Hecho que se notifica tras el commit (CU-33). {@code tipo}: CONFIRMADA, REPROGRAMADA o CANCELADA.
 * Lleva identificadores y la fecha de la cita, nunca datos personales ni clinicos.
 */
public record EventoCita(
        String tipo, String citaId, String pacienteId, String profesionalId, String centroId, String fecha, int horaInicio, String actorId) {}
