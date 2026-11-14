package com.appointy.notification.infrastructure.entry_points.dto;

import com.appointy.notification.domain.model.SolicitudCita;
import jakarta.validation.constraints.NotBlank;

/** Contrato fijado en la Fase 3 (docs/progreso.md). La validacion de formato la hace el dominio. */
public record NotificarCitaRequest(
        @NotBlank String tipo, @NotBlank String citaId, @NotBlank String pacienteId, String profesionalId, String centroId,
        @NotBlank String fecha, @NotBlank String horaInicio, String actorId) {

    public SolicitudCita aSolicitud() {
        return new SolicitudCita(tipo, citaId, pacienteId, profesionalId, centroId, fecha, horaInicio, actorId);
    }
}
