package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(UUID id);

    List<AuditLog> findByUserId(UUID userId, int page, int size);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, int page, int size);

    List<AuditLog> findByAction(String action, int page, int size);

    List<AuditLog> findByDateRange(java.time.Instant start, java.time.Instant end, int page, int size);

    long countByUserId(UUID userId);
}