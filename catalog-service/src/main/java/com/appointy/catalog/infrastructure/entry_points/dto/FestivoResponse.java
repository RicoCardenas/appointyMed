package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.Festivo;

public record FestivoResponse(String fecha, String nombre) {

    public static FestivoResponse de(Festivo f) {
        return new FestivoResponse(f.fecha(), f.nombre());
    }
}
