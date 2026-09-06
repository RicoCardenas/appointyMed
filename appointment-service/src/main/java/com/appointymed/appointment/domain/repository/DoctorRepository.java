package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;

public interface DoctorRepository {
    Doctor save(Doctor doctor);

    DomainOptional<Doctor> findById(DomainId id);

    DomainOptional<Doctor> findByUserId(DomainId id);

    DomainOptional<Doctor> findByMedicalLicense(String license);

    DomainList<Doctor> findBySpecialtyId(DomainId id);

    DomainList<Doctor> findAllActive();

    DomainList<Doctor> findAll();

    boolean existsByMedicalLicense(String license);

    boolean existsByUserId(DomainId id);
}
