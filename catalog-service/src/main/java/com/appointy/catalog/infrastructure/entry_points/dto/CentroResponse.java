package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Centro;

public record CentroResponse(String id, String nombre, String direccion, String ciudad, String telefono, boolean activo) {

    public static CentroResponse de(Centro c) {
        return new CentroResponse(c.id(), c.nombre(), c.direccion(), c.ciudad(), c.telefono(), c.activo());
    }
}
