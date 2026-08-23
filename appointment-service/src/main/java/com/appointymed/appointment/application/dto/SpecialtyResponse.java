package com.appointymed.appointment.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String name,
        String description,
        int consultationDurationMinutes,
        boolean active,
        Instant createdAt
) {}
