package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByPatientId(UUID patientId);
    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);
    List<Appointment> findByDoctorId(UUID doctorId);
    List<Appointment> findByDoctorIdAndDateRange(UUID doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findActiveAppointmentsByDoctorAndDate(UUID doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    boolean hasActiveAppointmentConflict(UUID doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeAppointmentId);
}
