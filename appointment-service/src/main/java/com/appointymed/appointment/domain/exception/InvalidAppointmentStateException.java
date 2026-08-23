package com.appointymed.appointment.domain.exception;

public class InvalidAppointmentStateException extends DomainException {
    public InvalidAppointmentStateException(String message) {
        super(message, "INVALID_APPOINTMENT_STATE");
    }
}
