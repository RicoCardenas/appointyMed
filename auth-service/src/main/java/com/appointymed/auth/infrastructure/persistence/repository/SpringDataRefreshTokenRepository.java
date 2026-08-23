package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);

    List<RefreshTokenJpaEntity> findByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.userId = ?1")
    void deleteByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < ?1")
    void deleteExpiredTokens(Instant now);

    long countByUserId(UUID userId);
}