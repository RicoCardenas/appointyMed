package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Specialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository {
    Specialty save(Specialty specialty);
    Optional<Specialty> findById(UUID id);
    Optional<Specialty> findByName(String name);
    List<Specialty> findAllActive();
    List<Specialty> findAll();
    boolean existsByName(String name);
}
