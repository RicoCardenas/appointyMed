package com.appointymed.auth.domain.model;

public final class AuditLog {

    private final DomainId id;
    private final DomainId userId;
    private final String action;
    private final String entityType;
    private final DomainId entityId;
    private final String oldValue;
    private final String newValue;
    private final String ipAddress;
    private final DomainInstant timestamp;

    private AuditLog(Builder b) {
        id = b.id;
        userId = b.userId;
        action = b.action;
        entityType = b.entityType;
        entityId = b.entityId;
        oldValue = b.oldValue;
        newValue = b.newValue;
        ipAddress = b.ipAddress;
        timestamp = b.timestamp;
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public DomainId getEntityId() {
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

    public DomainInstant getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private DomainId id, userId, entityId;
        private String action, entityType, oldValue, newValue, ipAddress;
        private DomainInstant timestamp;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder userId(DomainId v) {
            userId = v;
            return this;
        }

        public Builder action(String v) {
            action = v;
            return this;
        }

        public Builder entityType(String v) {
            entityType = v;
            return this;
        }

        public Builder entityId(DomainId v) {
            entityId = v;
            return this;
        }

        public Builder oldValue(String v) {
            oldValue = v;
            return this;
        }

        public Builder newValue(String v) {
            newValue = v;
            return this;
        }

        public Builder ipAddress(String v) {
            ipAddress = v;
            return this;
        }

        public Builder timestamp(DomainInstant v) {
            timestamp = v;
            return this;
        }

        public AuditLog build() {
            if (action == null || action.trim().isEmpty())
                throw new IllegalArgumentException("Audit action is required");
            return new AuditLog(this);
        }
    }
}
