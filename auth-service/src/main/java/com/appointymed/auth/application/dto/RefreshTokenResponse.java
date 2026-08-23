package com.appointymed.auth.application.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {}