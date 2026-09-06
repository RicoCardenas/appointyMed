package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Appointment;

public interface AppointmentEventPublisherPort {
    void publishAppointmentBooked(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String specialtyName);

    void publishAppointmentCancelled(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String reason);

    void publishAppointmentRescheduled(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String specialtyName);
}
