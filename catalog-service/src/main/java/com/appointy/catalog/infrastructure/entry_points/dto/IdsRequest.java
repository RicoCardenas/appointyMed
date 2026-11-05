package com.appointy.catalog.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Lista completa de ids que reemplaza a la actual; una lista vacia quita todas las asociaciones. */
public record IdsRequest(@NotNull @Size(max = 50) List<UUID> ids) {

    public String[] comoTexto() {
        return ids.stream().map(UUID::toString).toArray(String[]::new);
    }
}
