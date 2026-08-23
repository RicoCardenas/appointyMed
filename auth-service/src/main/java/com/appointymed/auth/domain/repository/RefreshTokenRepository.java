package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(UUID userId);

    RefreshToken save(RefreshToken refreshToken);

    void deleteById(UUID id);

    void deleteByUserId(UUID userId);

    void deleteExpiredTokens();

    long countByUserId(UUID userId);
}