package com.appointy.catalog.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CentroRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 200) String direccion,
        @NotBlank @Size(max = 100) String ciudad,
        @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "telefono invalido") String telefono) {}
