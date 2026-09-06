package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;

public interface DoctorScheduleRepository {
    DoctorSchedule save(DoctorSchedule schedule);

    DomainList<DoctorSchedule> saveAll(DomainList<DoctorSchedule> schedules);

    DomainList<DoctorSchedule> findByDoctorId(DomainId id);

    DomainOptional<DoctorSchedule> findByDoctorIdAndDayOfWeek(DomainId id, int dayOfWeek);

    void deleteByDoctorId(DomainId id);
}
