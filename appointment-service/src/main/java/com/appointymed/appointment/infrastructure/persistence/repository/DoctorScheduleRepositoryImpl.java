package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.DoctorScheduleJpaEntity;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DoctorScheduleRepositoryImpl implements DoctorScheduleRepository {
    private final SpringDataDoctorScheduleRepository springDataRepository;
    public DoctorScheduleRepositoryImpl(SpringDataDoctorScheduleRepository springDataRepository) { this.springDataRepository = springDataRepository; }
    @Override public DoctorSchedule save(DoctorSchedule schedule) { return toDomain(springDataRepository.save(toEntity(schedule))); }
    @Override public DomainList<DoctorSchedule> saveAll(DomainList<DoctorSchedule> schedules) {
        List<DoctorScheduleJpaEntity> entities = new ArrayList<>();
        for (int index = 0; index < schedules.size(); index++) entities.add(toEntity(schedules.get(index)));
        DomainList<DoctorSchedule> result = new DomainList<>();
        for (DoctorScheduleJpaEntity entity : springDataRepository.saveAll(entities)) result.add(toDomain(entity));
        return result;
    }
    @Override public DomainList<DoctorSchedule> findByDoctorId(DomainId id) { return list(springDataRepository.findByDoctorIdAndActiveTrue(AppointmentDomainValueMapper.toUuid(id))); }
    @Override public DomainOptional<DoctorSchedule> findByDoctorIdAndDayOfWeek(DomainId id, int dayOfWeek) {
        var value = springDataRepository.findByDoctorIdAndDayOfWeek(AppointmentDomainValueMapper.toUuid(id), dayOfWeek);
        return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty();
    }
    @Override public void deleteByDoctorId(DomainId id) { springDataRepository.deleteByDoctorId(AppointmentDomainValueMapper.toUuid(id)); }
    private DomainList<DoctorSchedule> list(List<DoctorScheduleJpaEntity> entities) { DomainList<DoctorSchedule> result = new DomainList<>(); for (DoctorScheduleJpaEntity entity : entities) result.add(toDomain(entity)); return result; }
    private DoctorScheduleJpaEntity toEntity(DoctorSchedule s) { return DoctorScheduleJpaEntity.builder().id(AppointmentDomainValueMapper.toUuid(s.getId())).doctorId(AppointmentDomainValueMapper.toUuid(s.getDoctorId())).dayOfWeek(s.getDayOfWeek()).startTime(AppointmentDomainValueMapper.toLocalTime(s.getStartTime())).endTime(AppointmentDomainValueMapper.toLocalTime(s.getEndTime())).slotDurationMinutes(s.getSlotDurationMinutes()).active(s.isActive()).build(); }
    private DoctorSchedule toDomain(DoctorScheduleJpaEntity e) { return DoctorSchedule.builder().id(AppointmentDomainValueMapper.toDomainId(e.getId())).doctorId(AppointmentDomainValueMapper.toDomainId(e.getDoctorId())).dayOfWeek(e.getDayOfWeek()).startTime(AppointmentDomainValueMapper.toDomainTime(e.getStartTime())).endTime(AppointmentDomainValueMapper.toDomainTime(e.getEndTime())).slotDurationMinutes(e.getSlotDurationMinutes()).active(e.isActive()).build(); }
}
