package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.AuditLog;
import com.appointymed.auth.domain.repository.AuditLogRepository;
import com.appointymed.auth.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final SpringDataAuditLogRepository springDataAuditLogRepository;

    public AuditLogRepositoryImpl(SpringDataAuditLogRepository springDataAuditLogRepository) {
        this.springDataAuditLogRepository = springDataAuditLogRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = toEntity(auditLog);
        AuditLogJpaEntity saved = springDataAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return springDataAuditLogRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<AuditLog> findByUserId(UUID userId, int page, int size) {
        return springDataAuditLogRepository.findByUserId(userId, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, int page, int size) {
        return springDataAuditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByAction(String action, int page, int size) {
        return springDataAuditLogRepository.findByAction(action, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByDateRange(Instant start, Instant end, int page, int size) {
        return springDataAuditLogRepository.findByTimestampBetween(start, end, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return springDataAuditLogRepository.countByUserId(userId);
    }

    private AuditLog toDomain(AuditLogJpaEntity entity) {
        return AuditLog.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .ipAddress(entity.getIpAddress())
                .timestamp(entity.getTimestamp())
                .build();
    }

    private AuditLogJpaEntity toEntity(AuditLog domain) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setAction(domain.getAction());
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(domain.getEntityId());
        entity.setOldValue(domain.getOldValue());
        entity.setNewValue(domain.getNewValue());
        entity.setIpAddress(domain.getIpAddress());
        entity.setTimestamp(domain.getTimestamp());
        return entity;
    }
}