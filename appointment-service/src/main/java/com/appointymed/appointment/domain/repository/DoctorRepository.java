package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Doctor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository {
    Doctor save(Doctor doctor);
    Optional<Doctor> findById(UUID id);
    Optional<Doctor> findByUserId(UUID userId);
    Optional<Doctor> findByMedicalLicense(String medicalLicense);
    List<Doctor> findBySpecialtyId(UUID specialtyId);
    List<Doctor> findAllActive();
    List<Doctor> findAll();
    boolean existsByMedicalLicense(String medicalLicense);
    boolean existsByUserId(UUID userId);
}
