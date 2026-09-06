package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.AuditLog;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.repository.AuditLogRepository;

public final class AuditOperationUseCase {
    private final AuditLogRepository auditLogRepository;

    public AuditOperationUseCase(AuditLogRepository r) {
        auditLogRepository = r;
    }

    public void execute(AuditCommand c) {
        auditLogRepository.save(
                AuditLog.builder()
                        .userId(c.userId())
                        .action(c.action())
                        .entityType(c.entityType())
                        .entityId(c.entityId())
                        .oldValue(c.oldValue())
                        .newValue(c.newValue())
                        .ipAddress(c.ipAddress())
                        .build());
    }

    public record AuditCommand(
            DomainId userId,
            String action,
            String entityType,
            DomainId entityId,
            String oldValue,
            String newValue,
            String ipAddress) {}
}
