package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.infrastructure.persistence.entity.DoctorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDoctorRepository extends JpaRepository<DoctorJpaEntity, UUID> {
    Optional<DoctorJpaEntity> findByUserId(UUID userId);
    Optional<DoctorJpaEntity> findByMedicalLicense(String medicalLicense);
    List<DoctorJpaEntity> findBySpecialtyIdAndActiveTrue(UUID specialtyId);
    List<DoctorJpaEntity> findByActiveTrue();
    boolean existsByMedicalLicense(String medicalLicense);
    boolean existsByUserId(UUID userId);
}
