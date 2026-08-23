package com.appointymed.appointment.application.dto;

import com.appointymed.appointment.domain.model.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterPatientRequest(
        UUID userId,

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @NotBlank(message = "Document number is required")
        @Size(max = 50, message = "Document number cannot exceed 50 characters")
        String documentNumber,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        String phone,

        LocalDate birthDate,

        String bloodType,

        String address,

        String emergencyContactName,

        String emergencyContactPhone
) {}
