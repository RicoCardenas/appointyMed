package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.Diagnostico;

public record DiagnosticoResponse(String codigo, String descripcion) {

    public static DiagnosticoResponse de(Diagnostico d) {
        return new DiagnosticoResponse(d.codigo(), d.descripcion());
    }
}
