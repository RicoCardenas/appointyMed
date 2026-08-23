package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.DoctorScheduleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DoctorScheduleRepositoryImpl implements DoctorScheduleRepository {
    private final SpringDataDoctorScheduleRepository springDataRepository;

    public DoctorScheduleRepositoryImpl(SpringDataDoctorScheduleRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public DoctorSchedule save(DoctorSchedule schedule) {
        DoctorScheduleJpaEntity entity = toEntity(schedule);
        DoctorScheduleJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<DoctorSchedule> saveAll(List<DoctorSchedule> schedules) {
        List<DoctorScheduleJpaEntity> entities = schedules.stream().map(this::toEntity).toList();
        return springDataRepository.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DoctorSchedule> findByDoctorId(UUID doctorId) {
        return springDataRepository.findByDoctorIdAndActiveTrue(doctorId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<DoctorSchedule> findByDoctorIdAndDayOfWeek(UUID doctorId, int dayOfWeek) {
        return springDataRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek).map(this::toDomain);
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        springDataRepository.deleteByDoctorId(doctorId);
    }

    private DoctorScheduleJpaEntity toEntity(DoctorSchedule s) {
        return DoctorScheduleJpaEntity.builder()
                .id(s.getId())
                .doctorId(s.getDoctorId())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .slotDurationMinutes(s.getSlotDurationMinutes())
                .active(s.isActive())
                .build();
    }

    private DoctorSchedule toDomain(DoctorScheduleJpaEntity e) {
        return DoctorSchedule.builder()
                .id(e.getId())
                .doctorId(e.getDoctorId())
                .dayOfWeek(e.getDayOfWeek())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .slotDurationMinutes(e.getSlotDurationMinutes())
                .active(e.isActive())
                .build();
    }
}
