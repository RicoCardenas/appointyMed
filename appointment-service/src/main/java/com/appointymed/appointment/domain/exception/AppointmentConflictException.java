package com.appointymed.appointment.domain.exception;

import java.time.LocalDateTime;

public class AppointmentConflictException extends DomainException {
    public AppointmentConflictException(String message) {
        super(message, "APPOINTMENT_CONFLICT");
    }

    public AppointmentConflictException(LocalDateTime dateTime) {
        super("The requested time slot at " + dateTime + " is not available or is already booked.", "APPOINTMENT_CONFLICT");
    }
}
