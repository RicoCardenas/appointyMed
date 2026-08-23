package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, UUID> {
    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.userId = ?1 ORDER BY a.timestamp DESC")
    Page<AuditLogJpaEntity> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.entityType = ?1 AND a.entityId = ?2 ORDER BY a.timestamp DESC")
    Page<AuditLogJpaEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.action = ?1 ORDER BY a.timestamp DESC")
    Page<AuditLogJpaEntity> findByAction(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.timestamp BETWEEN ?1 AND ?2 ORDER BY a.timestamp DESC")
    Page<AuditLogJpaEntity> findByTimestampBetween(Instant start, Instant end, Pageable pageable);

    long countByUserId(UUID userId);
}