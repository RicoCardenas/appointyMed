package com.appointy.catalog.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 300) String descripcion) {}
