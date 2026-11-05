package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import java.time.Instant;

public record EntradaClinicaResponse(
        String id, String pacienteId, String profesionalId, String citaId, String motivo, String diagnostico,
        String notas, String indicaciones, String corrigeAId, Instant creadoEn) {

    public static EntradaClinicaResponse de(EntradaClinica e) {
        return new EntradaClinicaResponse(e.id(), e.pacienteId(), e.profesionalId(), e.citaId(), e.motivo(), e.diagnostico(),
                e.notas(), e.indicaciones(), e.corrigeAId(), Instant.ofEpochMilli(e.creadoEn()));
    }
}
