package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Profesional;

public record ProfesionalResponse(String id, String usuarioId, String nombres, String apellidos, String registroMedico, boolean activo) {

    public static ProfesionalResponse de(Profesional p) {
        return new ProfesionalResponse(p.id(), p.usuarioId(), p.nombres(), p.apellidos(), p.registroMedico(), p.activo());
    }
}
