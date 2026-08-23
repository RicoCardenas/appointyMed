package com.appointymed.appointment.application.dto;

import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID doctorId,
        String doctorName,
        UUID patientId,
        String patientName,
        String patientPhone,
        UUID specialtyId,
        String specialtyName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime appointmentDateTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endDateTime,
        AppointmentStatus status,
        String reason,
        String cancellationReason,
        String notes,
        Instant createdAt
) {}
