package com.appointy.appointment.domain.model;

/** Fila append-only (RNF-13). {@code estadoAnterior} es null al crear la cita; las franjas, null si no cambian. */
public record HistorialCita(
        String id,
        String citaId,
        EstadoCita estadoAnterior,
        EstadoCita estadoNuevo,
        String usuarioId,
        String rolUsuario,
        String franjaAnteriorId,
        String franjaNuevaId,
        String detalle,
        long fecha) {}
