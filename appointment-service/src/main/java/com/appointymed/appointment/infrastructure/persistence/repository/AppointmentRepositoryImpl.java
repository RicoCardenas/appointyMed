package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.AppointmentJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AppointmentRepositoryImpl implements AppointmentRepository {
    private final SpringDataAppointmentRepository springDataRepository;

    public AppointmentRepositoryImpl(SpringDataAppointmentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentJpaEntity entity = AppointmentJpaEntity.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .specialtyId(appointment.getSpecialtyId())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .endDateTime(appointment.getEndDateTime())
                .status(appointment.getStatus().name())
                .reason(appointment.getReason())
                .cancellationReason(appointment.getCancellationReason())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
        AppointmentJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Appointment> findByPatientId(UUID patientId) {
        return springDataRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status) {
        return springDataRepository.findByPatientIdAndStatusOrderByAppointmentDateTimeDesc(patientId, status.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByDoctorId(UUID doctorId) {
        return springDataRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByDoctorIdAndDateRange(UUID doctorId, LocalDateTime start, LocalDateTime end) {
        return springDataRepository.findByDoctorIdAndDateRange(doctorId, start, end).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findActiveAppointmentsByDoctorAndDate(UUID doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return springDataRepository.findActiveAppointmentsByDoctorAndDate(doctorId, startOfDay, endOfDay).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean hasActiveAppointmentConflict(UUID doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeAppointmentId) {
        return springDataRepository.hasConflict(doctorId, startDateTime, endDateTime, excludeAppointmentId);
    }

    private Appointment toDomain(AppointmentJpaEntity e) {
        AppointmentStatus status;
        try {
            status = AppointmentStatus.valueOf(e.getStatus());
        } catch (Exception ex) {
            status = AppointmentStatus.SCHEDULED;
        }

        return Appointment.builder()
                .id(e.getId())
                .doctorId(e.getDoctorId())
                .patientId(e.getPatientId())
                .specialtyId(e.getSpecialtyId())
                .appointmentDateTime(e.getAppointmentDateTime())
                .endDateTime(e.getEndDateTime())
                .status(status)
                .reason(e.getReason())
                .cancellationReason(e.getCancellationReason())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
