package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.AppointmentJpaEntity;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppointmentRepositoryImpl implements AppointmentRepository {
    private final SpringDataAppointmentRepository springDataRepository;

    public AppointmentRepositoryImpl(SpringDataAppointmentRepository springDataRepository) { this.springDataRepository = springDataRepository; }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentJpaEntity entity = AppointmentJpaEntity.builder()
                .id(AppointmentDomainValueMapper.toUuid(appointment.getId()))
                .doctorId(AppointmentDomainValueMapper.toUuid(appointment.getDoctorId()))
                .patientId(AppointmentDomainValueMapper.toUuid(appointment.getPatientId()))
                .specialtyId(AppointmentDomainValueMapper.toUuid(appointment.getSpecialtyId()))
                .appointmentDateTime(AppointmentDomainValueMapper.toLocalDateTime(appointment.getAppointmentDateTime()))
                .endDateTime(AppointmentDomainValueMapper.toLocalDateTime(appointment.getEndDateTime()))
                .status(appointment.getStatus().name()).reason(appointment.getReason())
                .cancellationReason(appointment.getCancellationReason()).notes(appointment.getNotes())
                .createdAt(AppointmentDomainValueMapper.toInstant(appointment.getCreatedAt()))
                .updatedAt(AppointmentDomainValueMapper.toInstant(appointment.getUpdatedAt())).build();
        return toDomain(springDataRepository.save(entity));
    }

    @Override public DomainOptional<Appointment> findById(DomainId id) {
        var entity = springDataRepository.findById(AppointmentDomainValueMapper.toUuid(id));
        return entity.isPresent() ? DomainOptional.of(toDomain(entity.get())) : DomainOptional.empty();
    }
    @Override public DomainList<Appointment> findByPatientId(DomainId id) { return list(springDataRepository.findByPatientIdOrderByAppointmentDateTimeDesc(AppointmentDomainValueMapper.toUuid(id))); }
    @Override public DomainList<Appointment> findByPatientIdAndStatus(DomainId id, AppointmentStatus status) { return list(springDataRepository.findByPatientIdAndStatusOrderByAppointmentDateTimeDesc(AppointmentDomainValueMapper.toUuid(id), status.name())); }
    @Override public DomainList<Appointment> findByDoctorId(DomainId id) { return list(springDataRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(AppointmentDomainValueMapper.toUuid(id))); }
    @Override public DomainList<Appointment> findByDoctorIdAndDateRange(DomainId id, DomainDateTime start, DomainDateTime end) { return list(springDataRepository.findByDoctorIdAndDateRange(AppointmentDomainValueMapper.toUuid(id), AppointmentDomainValueMapper.toLocalDateTime(start), AppointmentDomainValueMapper.toLocalDateTime(end))); }
    @Override public DomainList<Appointment> findActiveAppointmentsByDoctorAndDate(DomainId id, DomainDateTime start, DomainDateTime end) { return list(springDataRepository.findActiveAppointmentsByDoctorAndDate(AppointmentDomainValueMapper.toUuid(id), AppointmentDomainValueMapper.toLocalDateTime(start), AppointmentDomainValueMapper.toLocalDateTime(end))); }
    @Override public boolean hasActiveAppointmentConflict(DomainId doctorId, DomainDateTime start, DomainDateTime end, DomainId excludeId) { return springDataRepository.hasConflict(AppointmentDomainValueMapper.toUuid(doctorId), AppointmentDomainValueMapper.toLocalDateTime(start), AppointmentDomainValueMapper.toLocalDateTime(end), AppointmentDomainValueMapper.toUuid(excludeId)); }

    private DomainList<Appointment> list(java.util.List<AppointmentJpaEntity> entities) { DomainList<Appointment> result = new DomainList<>(); for (AppointmentJpaEntity entity : entities) result.add(toDomain(entity)); return result; }
    private Appointment toDomain(AppointmentJpaEntity entity) {
        AppointmentStatus status;
        try { status = AppointmentStatus.valueOf(entity.getStatus()); } catch (Exception ignored) { status = AppointmentStatus.SCHEDULED; }
        return Appointment.builder().id(AppointmentDomainValueMapper.toDomainId(entity.getId()))
                .doctorId(AppointmentDomainValueMapper.toDomainId(entity.getDoctorId()))
                .patientId(AppointmentDomainValueMapper.toDomainId(entity.getPatientId()))
                .specialtyId(AppointmentDomainValueMapper.toDomainId(entity.getSpecialtyId()))
                .appointmentDateTime(AppointmentDomainValueMapper.toDomainDateTime(entity.getAppointmentDateTime()))
                .endDateTime(AppointmentDomainValueMapper.toDomainDateTime(entity.getEndDateTime()))
                .status(status).reason(entity.getReason()).cancellationReason(entity.getCancellationReason()).notes(entity.getNotes())
                .createdAt(AppointmentDomainValueMapper.toDomainInstant(entity.getCreatedAt()))
                .updatedAt(AppointmentDomainValueMapper.toDomainInstant(entity.getUpdatedAt())).build();
    }
}
