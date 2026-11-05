package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El texto completo corregido: la nueva entrada reemplaza a la anterior a efectos de lectura, que sigue visible. */
public record CorregirEntradaRequest(
        @NotBlank @Size(max = 5000) String motivo,
        @NotBlank @Size(max = 5000) String diagnostico,
        @Size(max = 5000) String notas,
        @Size(max = 5000) String indicaciones) {

    public ContenidoClinico aContenido() {
        return new ContenidoClinico(motivo, diagnostico, notas, indicaciones);
    }
}
