package com.appointymed.appointment.domain.model;

import com.appointymed.appointment.domain.exception.InvalidAppointmentStateException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private final UUID id;
    private final UUID doctorId;
    private final UUID patientId;
    private final UUID specialtyId;
    private LocalDateTime appointmentDateTime;
    private LocalDateTime endDateTime;
    private AppointmentStatus status;
    private final String reason;
    private String cancellationReason;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public Appointment(UUID id, UUID doctorId, UUID patientId, UUID specialtyId,
                       LocalDateTime appointmentDateTime, LocalDateTime endDateTime,
                       AppointmentStatus status, String reason, String cancellationReason,
                       String notes, Instant createdAt, Instant updatedAt) {
        if (appointmentDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Appointment start and end date times are required");
        }
        if (endDateTime.isBefore(appointmentDateTime) || endDateTime.isEqual(appointmentDateTime)) {
            throw new IllegalArgumentException("End date time must be strictly after start date time");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.specialtyId = specialtyId;
        this.appointmentDateTime = appointmentDateTime;
        this.endDateTime = endDateTime;
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
        this.reason = reason;
        this.cancellationReason = cancellationReason;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStateException("Appointment is already cancelled");
        }
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException("Cannot cancel an already completed appointment");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason != null ? reason : "Cancelled by user";
        this.updatedAt = Instant.now();
    }

    public void reschedule(LocalDateTime newStart, LocalDateTime newEnd) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStateException("Cannot reschedule a cancelled appointment. Please book a new one.");
        }
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException("Cannot reschedule an already completed appointment");
        }
        if (newEnd.isBefore(newStart) || newEnd.isEqual(newStart)) {
            throw new IllegalArgumentException("New end time must be after start time");
        }
        this.appointmentDateTime = newStart;
        this.endDateTime = newEnd;
        this.status = AppointmentStatus.RESCHEDULED;
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        if (this.status != AppointmentStatus.SCHEDULED && this.status != AppointmentStatus.RESCHEDULED) {
            throw new InvalidAppointmentStateException("Only scheduled or rescheduled appointments can be confirmed");
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void complete(String doctorNotes) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStateException("Cannot complete a cancelled appointment");
        }
        this.status = AppointmentStatus.COMPLETED;
        this.notes = doctorNotes;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getSpecialtyId() {
        return specialtyId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public LocalDateTime getEndDateTime() {
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
        private UUID doctorId;
        private UUID patientId;
        private UUID specialtyId;
        private LocalDateTime appointmentDateTime;
        private LocalDateTime endDateTime;
        private AppointmentStatus status = AppointmentStatus.SCHEDULED;
        private String reason;
        private String cancellationReason;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder doctorId(UUID doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder specialtyId(UUID specialtyId) {
            this.specialtyId = specialtyId;
            return this;
        }

        public Builder appointmentDateTime(LocalDateTime appointmentDateTime) {
            this.appointmentDateTime = appointmentDateTime;
            return this;
        }

        public Builder endDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
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

        public Appointment build() {
            return new Appointment(id, doctorId, patientId, specialtyId, appointmentDateTime, endDateTime, status, reason, cancellationReason, notes, createdAt, updatedAt);
        }
    }
}
