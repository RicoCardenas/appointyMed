package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegistrarAtencionRequest(
        @NotNull UUID citaId,
        @NotBlank @Size(max = 5000) String motivo,
        @NotBlank @Size(max = 5000) String diagnostico,
        @Size(max = 5000) String notas,
        @Size(max = 5000) String indicaciones) {

    public ContenidoClinico aContenido() {
        return new ContenidoClinico(motivo, diagnostico, notas, indicaciones);
    }
}
