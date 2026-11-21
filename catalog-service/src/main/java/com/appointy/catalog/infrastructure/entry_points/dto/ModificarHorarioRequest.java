package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.usecase.HorarioUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ModificarHorarioRequest(
        @NotNull UUID centroId,
        @Min(1) @Max(7) int diaSemana,
        @NotNull @Pattern(regexp = Horas.PATRON, message = "usa el formato HH:mm") String horaInicio,
        @NotNull @Pattern(regexp = Horas.PATRON, message = "usa el formato HH:mm") String horaFin,
        @Min(5) @Max(240) Integer duracionFranjaMin,
        @NotNull Boolean activo) {

    public HorarioUseCase.ModificarHorarioComando aComando(String horarioId) {
        return new HorarioUseCase.ModificarHorarioComando(
                horarioId, centroId.toString(), diaSemana, Horas.aMinutos(horaInicio), Horas.aMinutos(horaFin), duracionFranjaMin, activo);
    }
}
