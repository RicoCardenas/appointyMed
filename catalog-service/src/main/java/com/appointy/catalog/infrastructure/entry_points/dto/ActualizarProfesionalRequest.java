package com.appointy.catalog.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarProfesionalRequest(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9-]{3,30}$", message = "debe tener de 3 a 30 caracteres alfanumericos") String registroMedico) {}
