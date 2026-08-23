package com.appointymed.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AuditLog {
    private final UUID id;
    private final UUID userId;
    private final String action;
    private final String entityType;
    private final UUID entityId;
    private final String oldValue;
    private final String newValue;
    private final String ipAddress;
    private final Instant timestamp;

    private AuditLog(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.action = builder.action;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.oldValue = builder.oldValue;
        this.newValue = builder.newValue;
        this.ipAddress = builder.ipAddress;
        this.timestamp = builder.timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private UUID userId;
        private String action;
        private String entityType;
        private UUID entityId;
        private String oldValue;
        private String newValue;
        private String ipAddress;
        private Instant timestamp = Instant.now();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(UUID entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}