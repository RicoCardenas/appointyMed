package com.appointy.appointment.infrastructure.entry_points.dto;

import jakarta.validation.constraints.Size;

public record CancelarCitaRequest(@Size(max = 200) String motivo) {}
