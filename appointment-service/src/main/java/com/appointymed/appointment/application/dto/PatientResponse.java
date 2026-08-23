package com.appointymed.appointment.application.dto;

import com.appointymed.appointment.domain.model.DocumentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        UUID userId,
        DocumentType documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        LocalDate birthDate,
        String bloodType,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        Instant createdAt
) {}
