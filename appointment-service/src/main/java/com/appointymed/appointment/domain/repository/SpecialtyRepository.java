package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;
import com.appointymed.appointment.domain.model.Specialty;

public interface SpecialtyRepository {
    Specialty save(Specialty specialty);

    DomainOptional<Specialty> findById(DomainId id);

    DomainOptional<Specialty> findByName(String name);

    DomainList<Specialty> findAllActive();

    DomainList<Specialty> findAll();

    boolean existsByName(String name);
}
