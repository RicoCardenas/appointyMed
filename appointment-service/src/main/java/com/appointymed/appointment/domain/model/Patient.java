package com.appointymed.appointment.domain.model;

public final class Patient {
    private final DomainId id, userId;
    private final DocumentType documentType;
    private final String documentNumber,
            firstName,
            lastName,
            email,
            phone,
            bloodType,
            address,
            emergencyContactName,
            emergencyContactPhone;
    private final DomainDate birthDate;
    private final DomainInstant createdAt, updatedAt;

    public Patient(
            DomainId id,
            DomainId userId,
            DocumentType type,
            String documentNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            DomainDate birthDate,
            String bloodType,
            String address,
            String emergencyName,
            String emergencyPhone,
            DomainInstant createdAt,
            DomainInstant updatedAt) {
        this.id = id;
        this.userId = userId;
        documentType = type == null ? DocumentType.CC : type;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.bloodType = bloodType;
        this.address = address;
        emergencyContactName = emergencyName;
        emergencyContactPhone = emergencyPhone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getUserId() {
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

    public DomainDate getBirthDate() {
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
        private DomainId id, userId;
        private DocumentType type = DocumentType.CC;
        private String documentNumber,
                first,
                last,
                email,
                phone,
                blood,
                address,
                emergencyName,
                emergencyPhone;
        private DomainDate birth;
        private DomainInstant createdAt, updatedAt;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder userId(DomainId v) {
            userId = v;
            return this;
        }

        public Builder documentType(DocumentType v) {
            type = v;
            return this;
        }

        public Builder documentNumber(String v) {
            documentNumber = v;
            return this;
        }

        public Builder firstName(String v) {
            first = v;
            return this;
        }

        public Builder lastName(String v) {
            last = v;
            return this;
        }

        public Builder email(String v) {
            email = v;
            return this;
        }

        public Builder phone(String v) {
            phone = v;
            return this;
        }

        public Builder birthDate(DomainDate v) {
            birth = v;
            return this;
        }

        public Builder bloodType(String v) {
            blood = v;
            return this;
        }

        public Builder address(String v) {
            address = v;
            return this;
        }

        public Builder emergencyContactName(String v) {
            emergencyName = v;
            return this;
        }

        public Builder emergencyContactPhone(String v) {
            emergencyPhone = v;
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

        public Patient build() {
            return new Patient(
                    id,
                    userId,
                    type,
                    documentNumber,
                    first,
                    last,
                    email,
                    phone,
                    birth,
                    blood,
                    address,
                    emergencyName,
                    emergencyPhone,
                    createdAt,
                    updatedAt);
        }
    }
}
