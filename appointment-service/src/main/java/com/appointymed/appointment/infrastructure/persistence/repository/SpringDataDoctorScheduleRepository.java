package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.infrastructure.persistence.entity.DoctorScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDoctorScheduleRepository extends JpaRepository<DoctorScheduleJpaEntity, UUID> {
    List<DoctorScheduleJpaEntity> findByDoctorIdAndActiveTrue(UUID doctorId);
    List<DoctorScheduleJpaEntity> findByDoctorId(UUID doctorId);
    Optional<DoctorScheduleJpaEntity> findByDoctorIdAndDayOfWeek(UUID doctorId, int dayOfWeek);
    void deleteByDoctorId(UUID doctorId);
}
