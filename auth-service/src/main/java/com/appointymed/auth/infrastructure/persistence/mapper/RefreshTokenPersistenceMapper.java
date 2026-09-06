package com.appointymed.auth.infrastructure.persistence.mapper;

import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;

import java.util.UUID;
import java.time.Instant;

public class RefreshTokenPersistenceMapper {
    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) return null;

        return RefreshToken.builder()
                .id(toDomainId(entity.getId()))
                .token(entity.getToken())
                .userId(toDomainId(entity.getUserId()))
                .expiresAt(toDomainInstant(entity.getExpiresAt()))
                .createdAt(toDomainInstant(entity.getCreatedAt()))
                .revoked(entity.isRevoked())
                .build();
    }

    public static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        if (domain == null) return null;

        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(toUuid(domain.getId()));
        entity.setToken(domain.getToken());
        entity.setUserId(toUuid(domain.getUserId()));
        entity.setExpiresAt(toInstant(domain.getExpiresAt()));
        entity.setCreatedAt(toInstant(domain.getCreatedAt()));
        entity.setRevoked(domain.isRevoked());
        return entity;
    }

    private static DomainId toDomainId(UUID value) { return value == null ? null : DomainId.of(value.toString()); }
    private static UUID toUuid(DomainId value) { return value == null ? null : UUID.fromString(value.value()); }
    private static DomainInstant toDomainInstant(Instant value) { return value == null ? null : DomainInstant.ofEpochMillis(value.toEpochMilli()); }
    private static Instant toInstant(DomainInstant value) { return value == null ? null : Instant.ofEpochMilli(value.toEpochMillis()); }
}
