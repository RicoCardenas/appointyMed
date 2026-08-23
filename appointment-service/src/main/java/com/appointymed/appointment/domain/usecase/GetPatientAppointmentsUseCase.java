package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class GetPatientAppointmentsUseCase {
    private final AppointmentRepository appointmentRepository;

    public GetPatientAppointmentsUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> executeAll(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> executeUpcoming(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .filter(a -> a.getAppointmentDateTime().isAfter(LocalDateTime.now()) 
                        && a.getStatus() != AppointmentStatus.CANCELLED)
                .sorted((a1, a2) -> a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()))
                .toList();
    }

    public List<Appointment> executeHistory(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .filter(a -> a.getAppointmentDateTime().isBefore(LocalDateTime.now()) 
                        || a.getStatus() == AppointmentStatus.COMPLETED 
                        || a.getStatus() == AppointmentStatus.CANCELLED)
                .sorted((a1, a2) -> a2.getAppointmentDateTime().compareTo(a1.getAppointmentDateTime()))
                .toList();
    }
}
