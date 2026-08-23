package com.appointymed.appointment.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterDoctorRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Medical license is required")
        @Size(max = 50, message = "Medical license cannot exceed 50 characters")
        String medicalLicense,

        @NotNull(message = "Specialty ID is required")
        UUID specialtyId,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        String phone,

        String consultationRoom
) {}
