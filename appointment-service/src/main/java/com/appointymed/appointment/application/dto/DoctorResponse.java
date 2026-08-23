package com.appointymed.appointment.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        UUID userId,
        String medicalLicense,
        UUID specialtyId,
        String specialtyName,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String consultationRoom,
        boolean active,
        List<ScheduleResponse> schedules,
        Instant createdAt
) {}
