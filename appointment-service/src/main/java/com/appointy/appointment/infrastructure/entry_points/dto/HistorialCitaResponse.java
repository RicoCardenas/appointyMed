package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.HistorialCita;
import java.time.Instant;

public record HistorialCitaResponse(
        String id,
        String estadoAnterior,
        String estadoNuevo,
        String usuarioId,
        String rolUsuario,
        String franjaAnteriorId,
        String franjaNuevaId,
        String detalle,
        Instant fecha) {

    public static HistorialCitaResponse de(HistorialCita h) {
        return new HistorialCitaResponse(h.id(), h.estadoAnterior() == null ? null : h.estadoAnterior().name(), h.estadoNuevo().name(),
                h.usuarioId(), h.rolUsuario(), h.franjaAnteriorId(), h.franjaNuevaId(), h.detalle(), Instant.ofEpochMilli(h.fecha()));
    }
}
