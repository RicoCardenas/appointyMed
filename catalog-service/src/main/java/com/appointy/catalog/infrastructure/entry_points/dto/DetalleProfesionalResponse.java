package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.DetalleProfesional;
import java.util.List;

public record DetalleProfesionalResponse(ProfesionalResponse profesional, List<String> especialidadIds, List<String> centroIds) {

    public static DetalleProfesionalResponse de(DetalleProfesional d) {
        return new DetalleProfesionalResponse(ProfesionalResponse.de(d.profesional()), List.of(d.especialidadIds()), List.of(d.centroIds()));
    }
}
