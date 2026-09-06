package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.AuditLog;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);

    DomainOptional<AuditLog> findById(DomainId id);

    DomainList<AuditLog> findByUserId(DomainId userId, int page, int size);

    DomainList<AuditLog> findByEntityTypeAndEntityId(
            String entityType, DomainId entityId, int page, int size);

    DomainList<AuditLog> findByAction(String action, int page, int size);

    DomainList<AuditLog> findByDateRange(DomainInstant start, DomainInstant end, int page, int size);

    long countByUserId(DomainId userId);
}
