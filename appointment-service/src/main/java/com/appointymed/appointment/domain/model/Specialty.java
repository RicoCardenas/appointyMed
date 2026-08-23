package com.appointymed.appointment.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Specialty {
    private final UUID id;
    private final String name;
    private final String description;
    private final int consultationDurationMinutes;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Specialty(UUID id, String name, String description, int consultationDurationMinutes, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.consultationDurationMinutes = consultationDurationMinutes > 0 ? consultationDurationMinutes : 30;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getId() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private int consultationDurationMinutes = 30;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder consultationDurationMinutes(int consultationDurationMinutes) {
            this.consultationDurationMinutes = consultationDurationMinutes;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Specialty build() {
            return new Specialty(id, name, description, consultationDurationMinutes, active, createdAt, updatedAt);
        }
    }
}
