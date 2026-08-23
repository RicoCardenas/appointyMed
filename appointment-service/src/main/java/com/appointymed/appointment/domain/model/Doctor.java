package com.appointymed.appointment.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Doctor {
    private final UUID id;
    private final UUID userId;
    private final String medicalLicense;
    private final UUID specialtyId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String consultationRoom;
    private final boolean active;
    private final List<DoctorSchedule> schedules;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Doctor(UUID id, UUID userId, String medicalLicense, UUID specialtyId, String firstName, String lastName,
                  String email, String phone, String consultationRoom, boolean active,
                  List<DoctorSchedule> schedules, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.medicalLicense = medicalLicense;
        this.specialtyId = specialtyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.consultationRoom = consultationRoom;
        this.active = active;
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMedicalLicense() {
        return medicalLicense;
    }

    public UUID getSpecialtyId() {
        return specialtyId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getConsultationRoom() {
        return consultationRoom;
    }

    public boolean isActive() {
        return active;
    }

    public List<DoctorSchedule> getSchedules() {
        return schedules;
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
        private UUID userId;
        private String medicalLicense;
        private UUID specialtyId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String consultationRoom;
        private boolean active = true;
        private List<DoctorSchedule> schedules = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder medicalLicense(String medicalLicense) {
            this.medicalLicense = medicalLicense;
            return this;
        }

        public Builder specialtyId(UUID specialtyId) {
            this.specialtyId = specialtyId;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder consultationRoom(String consultationRoom) {
            this.consultationRoom = consultationRoom;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder schedules(List<DoctorSchedule> schedules) {
            this.schedules = schedules;
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

        public Doctor build() {
            return new Doctor(id, userId, medicalLicense, specialtyId, firstName, lastName, email, phone, consultationRoom, active, schedules, createdAt, updatedAt);
        }
    }
}
