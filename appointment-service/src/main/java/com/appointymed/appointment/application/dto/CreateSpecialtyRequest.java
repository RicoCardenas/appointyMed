package com.appointymed.appointment.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialtyRequest(
        @NotBlank(message = "Specialty name is required")
        @Size(max = 100, message = "Specialty name cannot exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Min(value = 10, message = "Consultation duration must be at least 10 minutes")
        int consultationDurationMinutes
) {}
