package com.appointymed.auth.infrastructure.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.appointymed.auth.domain.model.AuditLog;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
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
    private final ObjectMapper objectMapper;

    public AuditLogRepositoryImpl(SpringDataAuditLogRepository springDataAuditLogRepository, ObjectMapper objectMapper) {
        this.springDataAuditLogRepository = springDataAuditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = toEntity(auditLog);
        AuditLogJpaEntity saved = springDataAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public DomainOptional<AuditLog> findById(DomainId id) {
        Optional<AuditLogJpaEntity> entity = springDataAuditLogRepository.findById(UUID.fromString(id.value()));
        return entity.isPresent() ? DomainOptional.of(toDomain(entity.get())) : DomainOptional.empty();
    }

    @Override
    public DomainList<AuditLog> findByUserId(DomainId userId, int page, int size) {
        return toDomainList(springDataAuditLogRepository.findByUserId(UUID.fromString(userId.value()), PageRequest.of(page, size)).getContent());
    }

    @Override
    public DomainList<AuditLog> findByEntityTypeAndEntityId(String entityType, DomainId entityId, int page, int size) {
        return toDomainList(springDataAuditLogRepository.findByEntityTypeAndEntityId(entityType, UUID.fromString(entityId.value()), PageRequest.of(page, size)).getContent());
    }

    @Override
    public DomainList<AuditLog> findByAction(String action, int page, int size) {
        return toDomainList(springDataAuditLogRepository.findByAction(action, PageRequest.of(page, size)).getContent());
    }

    @Override
    public DomainList<AuditLog> findByDateRange(DomainInstant start, DomainInstant end, int page, int size) {
        return toDomainList(springDataAuditLogRepository.findByTimestampBetween(Instant.ofEpochMilli(start.toEpochMillis()), Instant.ofEpochMilli(end.toEpochMillis()), PageRequest.of(page, size)).getContent());
    }

    @Override
    public long countByUserId(DomainId userId) {
        return springDataAuditLogRepository.countByUserId(UUID.fromString(userId.value()));
    }

    private AuditLog toDomain(AuditLogJpaEntity entity) {
        return AuditLog.builder()
                .id(toDomainId(entity.getId()))
                .userId(toDomainId(entity.getUserId()))
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(toDomainId(entity.getEntityId()))
                .oldValue(jsonToString(entity.getOldValue()))
                .newValue(jsonToString(entity.getNewValue()))
                .ipAddress(entity.getIpAddress())
                .timestamp(toDomainInstant(entity.getTimestamp()))
                .build();
    }

    private AuditLogJpaEntity toEntity(AuditLog domain) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setId(toUuid(domain.getId()));
        entity.setUserId(toUuid(domain.getUserId()));
        entity.setAction(domain.getAction());
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(toUuid(domain.getEntityId()));
        entity.setOldValue(jsonToNode(domain.getOldValue()));
        entity.setNewValue(jsonToNode(domain.getNewValue()));
        entity.setIpAddress(domain.getIpAddress());
        entity.setTimestamp(domain.getTimestamp() == null ? null : Instant.ofEpochMilli(domain.getTimestamp().toEpochMillis()));
        return entity;
    }
    private String jsonToString(JsonNode value) {
        return value == null ? null : value.toString();
    }

    private JsonNode jsonToNode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Audit value must be valid JSON", ex);
        }
    }

    private DomainList<AuditLog> toDomainList(List<AuditLogJpaEntity> entities) {
        DomainList<AuditLog> result = new DomainList<>();
        for (AuditLogJpaEntity entity : entities) result.add(toDomain(entity));
        return result;
    }
    private static DomainId toDomainId(UUID value) { return value == null ? null : DomainId.of(value.toString()); }
    private static UUID toUuid(DomainId value) { return value == null ? null : UUID.fromString(value.value()); }
    private static DomainInstant toDomainInstant(Instant value) { return value == null ? null : DomainInstant.ofEpochMillis(value.toEpochMilli()); }

}
