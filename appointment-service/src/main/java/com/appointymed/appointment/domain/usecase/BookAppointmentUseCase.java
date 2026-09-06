package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
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

public final class BookAppointmentUseCase {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final SpecialtyRepository specialties;
    private final DoctorScheduleRepository schedules;
    private final AppointmentEventPublisherPort events;
    private final DomainClock clock;

    public BookAppointmentUseCase(
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

    public Appointment execute(BookAppointmentCommand c) {
        if (c.appointmentDateTime() == null || c.appointmentDateTime().isBefore(clock.now()))
            throw new DomainException("Cannot book an appointment in the past", "PAST_APPOINTMENT_DATE");
        Patient patient =
                patients
                        .findById(c.patientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Patient", c.patientId()));
        Doctor doctor =
                doctors
                        .findById(c.doctorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor", c.doctorId()));
        if (!doctor.isActive())
            throw new DomainException("Doctor is currently inactive", "DOCTOR_INACTIVE");
        DomainId specialtyId = c.specialtyId() == null ? doctor.getSpecialtyId() : c.specialtyId();
        if (!specialtyId.equals(doctor.getSpecialtyId()))
            throw new DomainException("Specialty does not belong to doctor", "DOCTOR_SPECIALTY_MISMATCH");
        Specialty specialty =
                specialties
                        .findById(specialtyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Specialty", specialtyId));
        DomainDateTime end =
                c.appointmentDateTime().plusMinutes(specialty.getConsultationDurationMinutes());
        DoctorSchedule schedule =
                schedules
                        .findByDoctorIdAndDayOfWeek(doctor.getId(), c.appointmentDateTime().getDayOfWeek())
                        .orElseThrow(
                                () ->
                                        new AppointmentConflictException(
                                                "Doctor has no active working schedule configured for this day of the"
                                                        + " week"));
        if (c.appointmentDateTime().toLocalTime().isBefore(schedule.getStartTime())
                || end.toLocalTime().isAfter(schedule.getEndTime()))
            throw new AppointmentConflictException(
                    "Appointment time is outside doctor's configured working hours");
        if (appointments.hasActiveAppointmentConflict(
                doctor.getId(), c.appointmentDateTime(), end, null))
            throw new AppointmentConflictException(c.appointmentDateTime());
        Appointment saved =
                appointments.save(
                        Appointment.builder()
                                .doctorId(doctor.getId())
                                .patientId(patient.getId())
                                .specialtyId(specialty.getId())
                                .appointmentDateTime(c.appointmentDateTime())
                                .endDateTime(end)
                                .status(AppointmentStatus.SCHEDULED)
                                .reason(c.reason())
                                .notes(c.notes())
                                .createdAt(clock.asInstant())
                                .updatedAt(clock.asInstant())
                                .build());
        events.publishAppointmentBooked(
                saved,
                patient.getEmail(),
                patient.getFullName(),
                doctor.getFullName(),
                specialty.getName());
        return saved;
    }

    public record BookAppointmentCommand(
            DomainId patientId,
            DomainId doctorId,
            DomainId specialtyId,
            DomainDateTime appointmentDateTime,
            String reason,
            String notes) {}
}
