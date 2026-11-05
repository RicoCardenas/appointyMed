package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.ParametroServicio;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/** {"parametros": {"horizonte_max_dias": "45"}}; los numeros sin comillas tambien se aceptan. */
public record ParametrosRequest(@NotEmpty Map<String, String> parametros) {

    public ParametroServicio[] comoParametros() {
        return parametros.entrySet().stream().map(e -> new ParametroServicio(e.getKey(), e.getValue(), null)).toArray(ParametroServicio[]::new);
    }
}
