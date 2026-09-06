package com.appointymed.appointment.domain.model;

public final class Specialty {
    private final DomainId id;
    private final String name, description;
    private final int consultationDurationMinutes;
    private final boolean active;
    private final DomainInstant createdAt, updatedAt;

    public Specialty(
            DomainId id,
            String name,
            String description,
            int duration,
            boolean active,
            DomainInstant createdAt,
            DomainInstant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        consultationDurationMinutes = duration > 0 ? duration : 30;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DomainId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getConsultationDurationMinutes() {
        return consultationDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public DomainInstant getCreatedAt() {
        return createdAt;
    }

    public DomainInstant getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DomainId id;
        private String name, description;
        private int duration = 30;
        private boolean active = true;
        private DomainInstant createdAt, updatedAt;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder name(String v) {
            name = v;
            return this;
        }

        public Builder description(String v) {
            description = v;
            return this;
        }

        public Builder consultationDurationMinutes(int v) {
            duration = v;
            return this;
        }

        public Builder active(boolean v) {
            active = v;
            return this;
        }

        public Builder createdAt(DomainInstant v) {
            createdAt = v;
            return this;
        }

        public Builder updatedAt(DomainInstant v) {
            updatedAt = v;
            return this;
        }

        public Specialty build() {
            return new Specialty(id, name, description, duration, active, createdAt, updatedAt);
        }
    }
}
