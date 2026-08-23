package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
public class GetDoctorAppointmentsUseCase {
    private final AppointmentRepository appointmentRepository;

    public GetDoctorAppointmentsUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> execute(UUID doctorId, LocalDate date) {
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            return appointmentRepository.findByDoctorIdAndDateRange(doctorId, start, end);
        }
        return appointmentRepository.findByDoctorId(doctorId);
    }
}
