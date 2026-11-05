package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Especialidad;

public record EspecialidadResponse(String id, String nombre, String descripcion, boolean activo) {

    public static EspecialidadResponse de(Especialidad e) {
        return new EspecialidadResponse(e.id(), e.nombre(), e.descripcion(), e.activo());
    }
}
