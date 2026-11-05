package com.appointy.appointment.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReprogramarCitaRequest(@NotNull UUID franjaId) {}
