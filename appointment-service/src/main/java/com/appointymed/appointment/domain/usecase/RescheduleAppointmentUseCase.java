package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
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
public class RescheduleAppointmentUseCase {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    public RescheduleAppointmentUseCase(AppointmentRepository appointmentRepository,
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

    public Appointment execute(UUID appointmentId, LocalDateTime newDateTime) {
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new DomainException("Cannot reschedule an appointment to a past date", "PAST_APPOINTMENT_DATE");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", appointment.getDoctorId()));

        Specialty specialty = specialtyRepository.findById(appointment.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", appointment.getSpecialtyId()));

        int durationMinutes = specialty.getConsultationDurationMinutes();
        LocalDateTime newEndDateTime = newDateTime.plusMinutes(durationMinutes);

        // Validate doctor working schedule
        int dayOfWeek = newDateTime.getDayOfWeek().getValue();
        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
                .orElseThrow(() -> new AppointmentConflictException("Doctor has no active working schedule configured for this day of the week"));

        LocalTime appointmentStartTime = newDateTime.toLocalTime();
        LocalTime appointmentEndTime = newEndDateTime.toLocalTime();

        if (appointmentStartTime.isBefore(schedule.getStartTime()) || appointmentEndTime.isAfter(schedule.getEndTime())) {
            throw new AppointmentConflictException("Appointment time is outside doctor's configured working hours (" + schedule.getStartTime() + " - " + schedule.getEndTime() + ")");
        }

        // Check for conflict excluding current appointment
        boolean conflictExists = appointmentRepository.hasActiveAppointmentConflict(
                doctor.getId(), newDateTime, newEndDateTime, appointment.getId()
        );

        if (conflictExists) {
            throw new AppointmentConflictException(newDateTime);
        }

        appointment.reschedule(newDateTime, newEndDateTime);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Publish event for notification microservice via SQS
        try {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient != null) {
                eventPublisher.publishAppointmentRescheduled(
                        updatedAppointment,
                        patient.getEmail(),
                        patient.getFullName(),
                        doctor.getFullName(),
                        specialty.getName()
                );
            }
        } catch (Exception e) {
            // Log warning
        }

        return updatedAppointment;
    }
}
