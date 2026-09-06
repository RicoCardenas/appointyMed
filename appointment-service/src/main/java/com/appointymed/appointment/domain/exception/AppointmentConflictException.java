package com.appointymed.appointment.domain.exception;

import com.appointymed.appointment.domain.model.DomainDateTime;

public final class AppointmentConflictException extends DomainException {
    public AppointmentConflictException(String message) {
        super(message, "APPOINTMENT_CONFLICT");
    }

    public AppointmentConflictException(DomainDateTime dateTime) {
        super(
                "The requested time slot at epoch "
                        + dateTime.toEpochMillis()
                        + " is not available or is already booked.",
                "APPOINTMENT_CONFLICT");
    }
}
