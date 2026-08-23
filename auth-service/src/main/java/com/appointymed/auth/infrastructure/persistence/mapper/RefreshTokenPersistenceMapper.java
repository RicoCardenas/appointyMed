package com.appointymed.auth.infrastructure.persistence.mapper;

import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;

import java.util.UUID;

public class RefreshTokenPersistenceMapper {
    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) return null;

        return RefreshToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .userId(entity.getUserId())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .revoked(entity.isRevoked())
                .build();
    }

    public static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        if (domain == null) return null;

        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(domain.getId());
        entity.setToken(domain.getToken());
        entity.setUserId(domain.getUserId());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setRevoked(domain.isRevoked());
        return entity;
    }
}