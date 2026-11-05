package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Profesional;

public record ProfesionalInternoResponse(String profesionalId, boolean activo) {

    public static ProfesionalInternoResponse de(Profesional p) {
        return new ProfesionalInternoResponse(p.id(), p.activo());
    }
}
