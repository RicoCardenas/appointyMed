package com.appointymed.auth.application.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String fullName,
        List<String> roles
) {}