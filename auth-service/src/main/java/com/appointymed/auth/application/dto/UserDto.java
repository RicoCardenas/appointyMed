package com.appointymed.auth.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String fullName,
        String phone,
        String status,
        boolean emailVerified,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}