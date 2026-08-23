package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JwtTokenProvider {
    String generateAccessToken(User user);
    String generateRefreshToken();
    Instant getRefreshTokenExpiry();
    UUID getUserIdFromAccessToken(String token);
    UUID getUserIdFromRefreshToken(String token);
    List<String> getRolesFromAccessToken(String token);
}