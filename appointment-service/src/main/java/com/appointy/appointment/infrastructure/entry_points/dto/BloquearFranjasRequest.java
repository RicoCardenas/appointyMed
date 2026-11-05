package com.appointy.appointment.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record BloquearFranjasRequest(@NotNull UUID profesionalId, @NotNull UUID centroId, @NotNull LocalDate fechaInicio, @NotNull LocalDate fechaFin) {}
