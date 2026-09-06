package com.appointymed.appointment.domain.model;

public final class Doctor {
    private final DomainId id, userId, specialtyId;
    private final String medicalLicense, firstName, lastName, email, phone, consultationRoom;
    private final boolean active;
    private final DomainList<DoctorSchedule> schedules;
    private final DomainInstant createdAt, updatedAt;

    public Doctor(
            DomainId id,
            DomainId userId,
            String license,
            DomainId specialtyId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String room,
            boolean active,
            DomainList<DoctorSchedule> schedules,
            DomainInstant createdAt,
            DomainInstant updatedAt) {
        this.id = id;
        this.userId = userId;
        medicalLicense = license;
        this.specialtyId = specialtyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        consultationRoom = room;
        this.active = active;
        this.schedules = schedules == null ? new DomainList<>() : schedules;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getUserId() {
        return userId;
    }

    public String getMedicalLicense() {
        return medicalLicense;
    }

    public DomainId getSpecialtyId() {
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

    public DomainList<DoctorSchedule> getSchedules() {
        return schedules;
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
        private DomainId id, userId, specialtyId;
        private String license, first, last, email, phone, room;
        private boolean active = true;
        private DomainList<DoctorSchedule> schedules = new DomainList<>();
        private DomainInstant createdAt, updatedAt;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder userId(DomainId v) {
            userId = v;
            return this;
        }

        public Builder medicalLicense(String v) {
            license = v;
            return this;
        }

        public Builder specialtyId(DomainId v) {
            specialtyId = v;
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

        public Builder consultationRoom(String v) {
            room = v;
            return this;
        }

        public Builder active(boolean v) {
            active = v;
            return this;
        }

        public Builder schedules(DomainList<DoctorSchedule> v) {
            schedules = v;
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

        public Doctor build() {
            return new Doctor(
                    id,
                    userId,
                    license,
                    specialtyId,
                    first,
                    last,
                    email,
                    phone,
                    room,
                    active,
                    schedules,
                    createdAt,
                    updatedAt);
        }
    }
}
