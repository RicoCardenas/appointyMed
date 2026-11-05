package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Bloqueo;
import java.time.Instant;

public record BloqueoResponse(
        String id, String profesionalId, String centroId, String fechaInicio, String fechaFin, String motivo, String creadoPor, Instant creadoEn) {

    public static BloqueoResponse de(Bloqueo b) {
        return new BloqueoResponse(
                b.id(), b.profesionalId(), b.centroId(), b.fechaInicio(), b.fechaFin(), b.motivo(), b.creadoPor(), Instant.ofEpochMilli(b.creadoEn()));
    }
}
