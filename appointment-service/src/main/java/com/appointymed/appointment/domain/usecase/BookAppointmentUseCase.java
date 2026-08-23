package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.AppointmentEventPublisherPort;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import com.appointymed.appointment.domain.repository.PatientRepository;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Component
public class BookAppointmentUseCase {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    public BookAppointmentUseCase(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  SpecialtyRepository specialtyRepository,
                                  DoctorScheduleRepository doctorScheduleRepository,
                                  AppointmentEventPublisherPort eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(BookAppointmentCommand command) {
        if (command.appointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new DomainException("Cannot book an appointment in the past", "PAST_APPOINTMENT_DATE");
        }

        Patient patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", command.patientId()));

        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", command.doctorId()));

        if (!doctor.isActive()) {
            throw new DomainException("Doctor is currently inactive", "DOCTOR_INACTIVE");
        }

        Specialty specialty = specialtyRepository.findById(command.specialtyId() != null ? command.specialtyId() : doctor.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", doctor.getSpecialtyId()));

        // Calculate end time
        int durationMinutes = specialty.getConsultationDurationMinutes();
        LocalDateTime endDateTime = command.appointmentDateTime().plusMinutes(durationMinutes);

        // Validate doctor working schedule
        int dayOfWeek = command.appointmentDateTime().getDayOfWeek().getValue();
        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
                .orElseThrow(() -> new AppointmentConflictException("Doctor has no active working schedule configured for this day of the week"));

        LocalTime appointmentStartTime = command.appointmentDateTime().toLocalTime();
        LocalTime appointmentEndTime = endDateTime.toLocalTime();

        if (appointmentStartTime.isBefore(schedule.getStartTime()) || appointmentEndTime.isAfter(schedule.getEndTime())) {
            throw new AppointmentConflictException("Appointment time is outside doctor's configured working hours (" + schedule.getStartTime() + " - " + schedule.getEndTime() + ")");
        }

        // Check for conflicting appointments
        boolean conflictExists = appointmentRepository.hasActiveAppointmentConflict(
                doctor.getId(), command.appointmentDateTime(), endDateTime, null
        );

        if (conflictExists) {
            throw new AppointmentConflictException(command.appointmentDateTime());
        }

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .specialtyId(specialty.getId())
                .appointmentDateTime(command.appointmentDateTime())
                .endDateTime(endDateTime)
                .status(AppointmentStatus.SCHEDULED)
                .reason(command.reason())
                .notes(command.notes())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Publish event for notification microservice via SQS
        try {
            eventPublisher.publishAppointmentBooked(
                    savedAppointment,
                    patient.getEmail(),
                    patient.getFullName(),
                    doctor.getFullName(),
                    specialty.getName()
            );
        } catch (Exception e) {
            // Log but don't break the transaction
        }

        return savedAppointment;
    }

    public record BookAppointmentCommand(
            UUID patientId,
            UUID doctorId,
            UUID specialtyId,
            LocalDateTime appointmentDateTime,
            String reason,
            String notes
    ) {}
}
