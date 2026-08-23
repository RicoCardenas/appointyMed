package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.DoctorSchedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorScheduleRepository {
    DoctorSchedule save(DoctorSchedule schedule);
    List<DoctorSchedule> saveAll(List<DoctorSchedule> schedules);
    List<DoctorSchedule> findByDoctorId(UUID doctorId);
    Optional<DoctorSchedule> findByDoctorIdAndDayOfWeek(UUID doctorId, int dayOfWeek);
    void deleteByDoctorId(UUID doctorId);
}
