package com.appointymed.appointment.domain.model;

import com.appointymed.appointment.domain.exception.InvalidAppointmentStateException;

public final class Appointment {
    private final DomainId id, doctorId, patientId, specialtyId;
    private DomainDateTime appointmentDateTime, endDateTime;
    private AppointmentStatus status;
    private final String reason;
    private String cancellationReason, notes;
    private final DomainInstant createdAt;
    private DomainInstant updatedAt;

    public Appointment(
            DomainId id,
            DomainId doctorId,
            DomainId patientId,
            DomainId specialtyId,
            DomainDateTime start,
            DomainDateTime end,
            AppointmentStatus status,
            String reason,
            String cancellationReason,
            String notes,
            DomainInstant createdAt,
            DomainInstant updatedAt) {
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException(
                    "Appointment start and end date times are required and end must be after start");
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.specialtyId = specialtyId;
        appointmentDateTime = start;
        endDateTime = end;
        this.status = status == null ? AppointmentStatus.SCHEDULED : status;
        this.reason = reason;
        this.cancellationReason = cancellationReason;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void cancel(String reason, DomainInstant now) {
        if (status == AppointmentStatus.CANCELLED)
            throw new InvalidAppointmentStateException("Appointment is already cancelled");
        if (status == AppointmentStatus.COMPLETED)
            throw new InvalidAppointmentStateException("Cannot cancel an already completed appointment");
        status = AppointmentStatus.CANCELLED;
        cancellationReason = reason == null || reason.trim().isEmpty() ? "Cancelled by user" : reason;
        updatedAt = now;
    }

    public void reschedule(DomainDateTime start, DomainDateTime end, DomainInstant now) {
        if (status == AppointmentStatus.CANCELLED)
            throw new InvalidAppointmentStateException(
                    "Cannot reschedule a cancelled appointment. Please book a new one.");
        if (status == AppointmentStatus.COMPLETED)
            throw new InvalidAppointmentStateException(
                    "Cannot reschedule an already completed appointment");
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("New end time must be after start time");
        appointmentDateTime = start;
        endDateTime = end;
        status = AppointmentStatus.RESCHEDULED;
        updatedAt = now;
    }

    public void confirm(DomainInstant now) {
        if (status != AppointmentStatus.SCHEDULED && status != AppointmentStatus.RESCHEDULED)
            throw new InvalidAppointmentStateException(
                    "Only scheduled or rescheduled appointments can be confirmed");
        status = AppointmentStatus.CONFIRMED;
        updatedAt = now;
    }

    public void complete(String doctorNotes, DomainInstant now) {
        if (status == AppointmentStatus.CANCELLED)
            throw new InvalidAppointmentStateException("Cannot complete a cancelled appointment");
        status = AppointmentStatus.COMPLETED;
        notes = doctorNotes;
        updatedAt = now;
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getDoctorId() {
        return doctorId;
    }

    public DomainId getPatientId() {
        return patientId;
    }

    public DomainId getSpecialtyId() {
        return specialtyId;
    }

    public DomainDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public DomainDateTime getEndDateTime() {
        return endDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getNotes() {
        return notes;
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
        private DomainId id, doctorId, patientId, specialtyId;
        private DomainDateTime start, end;
        private AppointmentStatus status = AppointmentStatus.SCHEDULED;
        private String reason, cancellationReason, notes;
        private DomainInstant createdAt, updatedAt;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder doctorId(DomainId v) {
            doctorId = v;
            return this;
        }

        public Builder patientId(DomainId v) {
            patientId = v;
            return this;
        }

        public Builder specialtyId(DomainId v) {
            specialtyId = v;
            return this;
        }

        public Builder appointmentDateTime(DomainDateTime v) {
            start = v;
            return this;
        }

        public Builder endDateTime(DomainDateTime v) {
            end = v;
            return this;
        }

        public Builder status(AppointmentStatus v) {
            status = v;
            return this;
        }

        public Builder reason(String v) {
            reason = v;
            return this;
        }

        public Builder cancellationReason(String v) {
            cancellationReason = v;
            return this;
        }

        public Builder notes(String v) {
            notes = v;
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

        public Appointment build() {
            return new Appointment(
                    id,
                    doctorId,
                    patientId,
                    specialtyId,
                    start,
                    end,
                    status,
                    reason,
                    cancellationReason,
                    notes,
                    createdAt,
                    updatedAt);
        }
    }
}
