package com.appointymed.appointment.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Patient {
    private final UUID id;
    private final UUID userId;
    private final DocumentType documentType;
    private final String documentNumber;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final LocalDate birthDate;
    private final String bloodType;
    private final String address;
    private final String emergencyContactName;
    private final String emergencyContactPhone;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Patient(UUID id, UUID userId, DocumentType documentType, String documentNumber,
                   String firstName, String lastName, String email, String phone,
                   LocalDate birthDate, String bloodType, String address,
                   String emergencyContactName, String emergencyContactPhone,
                   Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.documentType = documentType != null ? documentType : DocumentType.CC;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.bloodType = bloodType;
        this.address = address;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String getAddress() {
        return address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
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
        private DocumentType documentType = DocumentType.CC;
        private String documentNumber;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String bloodType;
        private String address;
        private String emergencyContactName;
        private String emergencyContactPhone;
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

        public Builder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder documentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
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

        public Builder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder bloodType(String bloodType) {
            this.bloodType = bloodType;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder emergencyContactName(String emergencyContactName) {
            this.emergencyContactName = emergencyContactName;
            return this;
        }

        public Builder emergencyContactPhone(String emergencyContactPhone) {
            this.emergencyContactPhone = emergencyContactPhone;
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

        public Patient build() {
            return new Patient(id, userId, documentType, documentNumber, firstName, lastName, email, phone, birthDate, bloodType, address, emergencyContactName, emergencyContactPhone, createdAt, updatedAt);
        }
    }
}
