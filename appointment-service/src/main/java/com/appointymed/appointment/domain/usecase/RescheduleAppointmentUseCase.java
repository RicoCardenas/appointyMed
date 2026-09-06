package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.DomainClock;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.AppointmentEventPublisherPort;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import com.appointymed.appointment.domain.repository.PatientRepository;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;

public final class RescheduleAppointmentUseCase {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final SpecialtyRepository specialties;
    private final DoctorScheduleRepository schedules;
    private final AppointmentEventPublisherPort events;
    private final DomainClock clock;

    public RescheduleAppointmentUseCase(
            AppointmentRepository a,
            DoctorRepository d,
            PatientRepository p,
            SpecialtyRepository s,
            DoctorScheduleRepository ds,
            AppointmentEventPublisherPort e,
            DomainClock c) {
        appointments = a;
        doctors = d;
        patients = p;
        specialties = s;
        schedules = ds;
        events = e;
        clock = c;
    }

    public Appointment execute(DomainId id, DomainDateTime start) {
        if (start == null || start.isBefore(clock.now()))
            throw new DomainException(
                    "Cannot reschedule an appointment to a past date", "PAST_APPOINTMENT_DATE");
        Appointment appointment =
                appointments
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        Doctor doctor =
                doctors
                        .findById(appointment.getDoctorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor", appointment.getDoctorId()));
        Specialty specialty =
                specialties
                        .findById(appointment.getSpecialtyId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Specialty", appointment.getSpecialtyId()));
        DomainDateTime end = start.plusMinutes(specialty.getConsultationDurationMinutes());
        DoctorSchedule schedule =
                schedules
                        .findByDoctorIdAndDayOfWeek(doctor.getId(), start.getDayOfWeek())
                        .orElseThrow(
                                () ->
                                        new AppointmentConflictException(
                                                "Doctor has no active working schedule configured for this day of the"
                                                        + " week"));
        if (start.toLocalTime().isBefore(schedule.getStartTime())
                || end.toLocalTime().isAfter(schedule.getEndTime()))
            throw new AppointmentConflictException(
                    "Appointment time is outside doctor's configured working hours");
        if (appointments.hasActiveAppointmentConflict(doctor.getId(), start, end, appointment.getId()))
            throw new AppointmentConflictException(start);
        appointment.reschedule(start, end, clock.asInstant());
        Appointment saved = appointments.save(appointment);
        Patient patient =
                patients
                        .findById(saved.getPatientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Patient", saved.getPatientId()));
        events.publishAppointmentRescheduled(
                saved,
                patient.getEmail(),
                patient.getFullName(),
                doctor.getFullName(),
                specialty.getName());
        return saved;
    }
}
