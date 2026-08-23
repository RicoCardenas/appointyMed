package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.AuditLog;
import com.appointymed.auth.domain.repository.AuditLogRepository;

import java.util.UUID;

@Component
public class AuditOperationUseCase {
    private final AuditLogRepository auditLogRepository;

    public AuditOperationUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void execute(AuditCommand command) {
        AuditLog auditLog = AuditLog.builder()
                .userId(command.userId())
                .action(command.action())
                .entityType(command.entityType())
                .entityId(command.entityId())
                .oldValue(command.oldValue())
                .newValue(command.newValue())
                .ipAddress(command.ipAddress())
                .build();

        auditLogRepository.save(auditLog);
    }

    public record AuditCommand(
            UUID userId,
            String action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress
    ) {}
}