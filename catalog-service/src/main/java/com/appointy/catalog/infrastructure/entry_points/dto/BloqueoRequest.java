package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.usecase.HorarioUseCase;
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

    public HorarioUseCase.BloquearFranjasComando aComando(String actorId) {
        return new HorarioUseCase.BloquearFranjasComando(
                actorId, profesionalId.toString(), centroId.toString(), fechaInicio.toString(), fechaFin.toString(), motivo);
    }
}
