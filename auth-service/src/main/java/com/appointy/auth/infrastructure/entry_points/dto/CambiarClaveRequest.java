package com.appointy.auth.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarClaveRequest(@NotBlank @Size(max = 72) String claveActual, @NotBlank @Size(max = 72) String claveNueva) {}
