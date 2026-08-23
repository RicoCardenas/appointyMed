package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {
    Patient save(Patient patient);
    Optional<Patient> findById(UUID id);
    Optional<Patient> findByUserId(UUID userId);
    Optional<Patient> findByDocumentNumber(String documentNumber);
    List<Patient> findAll(int page, int size);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByEmail(String email);
}
