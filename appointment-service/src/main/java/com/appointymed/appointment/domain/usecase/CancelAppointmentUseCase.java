package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DomainClock;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.AppointmentEventPublisherPort;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.PatientRepository;

public final class CancelAppointmentUseCase {
    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentEventPublisherPort events;
    private final DomainClock clock;

    public CancelAppointmentUseCase(
            AppointmentRepository a,
            PatientRepository p,
            DoctorRepository d,
            AppointmentEventPublisherPort e,
            DomainClock c) {
        appointments = a;
        patients = p;
        doctors = d;
        events = e;
        clock = c;
    }

    public Appointment execute(DomainId id, String reason) {
        Appointment appointment =
                appointments
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        appointment.cancel(reason, clock.asInstant());
        Patient patient =
                patients
                        .findById(appointment.getPatientId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Patient", appointment.getPatientId()));
        Doctor doctor =
                doctors
                        .findById(appointment.getDoctorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor", appointment.getDoctorId()));
        Appointment saved = appointments.save(appointment);
        events.publishAppointmentCancelled(
                saved,
                patient.getEmail(),
                patient.getFullName(),
                doctor.getFullName(),
                saved.getCancellationReason());
        return saved;
    }
}
