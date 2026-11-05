package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.usecase.BloquearFranjasUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record BloqueoRequest(
        @NotNull UUID profesionalId,
        @NotNull UUID centroId,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        @NotBlank @Size(max = 200) String motivo) {

    public BloquearFranjasUseCase.Comando aComando(String actorId) {
        return new BloquearFranjasUseCase.Comando(
                actorId, profesionalId.toString(), centroId.toString(), fechaInicio.toString(), fechaFin.toString(), motivo);
    }
}
