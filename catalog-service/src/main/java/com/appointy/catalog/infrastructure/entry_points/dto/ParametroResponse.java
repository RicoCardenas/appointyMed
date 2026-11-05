package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.ParametroServicio;

public record ParametroResponse(String clave, String valor, String descripcion) {

    public static ParametroResponse de(ParametroServicio p) {
        return new ParametroResponse(p.clave(), p.valor(), p.descripcion());
    }
}
