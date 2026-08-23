package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.infrastructure.persistence.entity.AppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAppointmentRepository extends JpaRepository<AppointmentJpaEntity, UUID> {
    List<AppointmentJpaEntity> findByPatientIdOrderByAppointmentDateTimeDesc(UUID patientId);
    List<AppointmentJpaEntity> findByPatientIdAndStatusOrderByAppointmentDateTimeDesc(UUID patientId, String status);
    List<AppointmentJpaEntity> findByDoctorIdOrderByAppointmentDateTimeAsc(UUID doctorId);

    @Query("SELECT a FROM AppointmentJpaEntity a WHERE a.doctorId = :doctorId AND a.appointmentDateTime >= :start AND a.appointmentDateTime <= :end ORDER BY a.appointmentDateTime ASC")
    List<AppointmentJpaEntity> findByDoctorIdAndDateRange(@Param("doctorId") UUID doctorId,
                                                          @Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);

    @Query("SELECT a FROM AppointmentJpaEntity a WHERE a.doctorId = :doctorId AND a.status <> 'CANCELLED' AND a.appointmentDateTime >= :startOfDay AND a.appointmentDateTime <= :endOfDay ORDER BY a.appointmentDateTime ASC")
    List<AppointmentJpaEntity> findActiveAppointmentsByDoctorAndDate(@Param("doctorId") UUID doctorId,
                                                                    @Param("startOfDay") LocalDateTime startOfDay,
                                                                    @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(a) > 0 FROM AppointmentJpaEntity a WHERE a.doctorId = :doctorId AND a.status <> 'CANCELLED' AND ((a.appointmentDateTime < :endDateTime AND a.endDateTime > :startDateTime)) AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean hasConflict(@Param("doctorId") UUID doctorId,
                        @Param("startDateTime") LocalDateTime startDateTime,
                        @Param("endDateTime") LocalDateTime endDateTime,
                        @Param("excludeId") UUID excludeId);
}
