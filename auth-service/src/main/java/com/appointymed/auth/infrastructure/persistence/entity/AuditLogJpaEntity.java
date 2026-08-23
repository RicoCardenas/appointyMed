package com.appointymed.auth.infrastructure.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogJpaEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "UUID")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "JSONB")
    private JsonNode oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "JSONB")
    private JsonNode newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}