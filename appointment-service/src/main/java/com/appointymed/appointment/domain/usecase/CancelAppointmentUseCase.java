package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.AppointmentEventPublisherPort;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CancelAppointmentUseCase {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    public CancelAppointmentUseCase(AppointmentRepository appointmentRepository,
                                   DoctorRepository doctorRepository,
                                   PatientRepository patientRepository,
                                   AppointmentEventPublisherPort eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        appointment.cancel(reason);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Publish event for notification microservice via SQS
        try {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);

            if (patient != null && doctor != null) {
                eventPublisher.publishAppointmentCancelled(
                        updatedAppointment,
                        patient.getEmail(),
                        patient.getFullName(),
                        doctor.getFullName(),
                        reason
                );
            }
        } catch (Exception e) {
            // Log warning
        }

        return updatedAppointment;
    }
}
