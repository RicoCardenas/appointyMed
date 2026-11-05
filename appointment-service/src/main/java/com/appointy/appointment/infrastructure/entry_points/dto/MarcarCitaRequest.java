package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.EstadoCita;
import jakarta.validation.constraints.NotNull;

/** {@code estado}: COMPLETADA o NO_ASISTIDA. */
public record MarcarCitaRequest(@NotNull EstadoCita estado) {}
