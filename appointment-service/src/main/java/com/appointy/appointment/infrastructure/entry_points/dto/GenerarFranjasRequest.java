package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.FranjaNueva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Contrato con catalog (Fase 2): fechas ISO y horas "HH:mm". */
public record GenerarFranjasRequest(
        @NotNull UUID profesionalId, @NotNull UUID centroId, @NotEmpty @Size(max = 20000) List<@Valid @NotNull Item> franjas) {

    public record Item(@NotNull LocalDate fecha, @NotNull @Pattern(regexp = Horas.PATRON) String horaInicio,
            @NotNull @Pattern(regexp = Horas.PATRON) String horaFin) {}

    public FranjaNueva[] aDominio() {
        return franjas.stream()
                .map(f -> new FranjaNueva(f.fecha().toString(), Horas.aMinutos(f.horaInicio()), Horas.aMinutos(f.horaFin())))
                .toArray(FranjaNueva[]::new);
    }
}
