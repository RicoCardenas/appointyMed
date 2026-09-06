package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;
import com.appointymed.appointment.domain.model.Patient;

public interface PatientRepository {
    Patient save(Patient patient);

    DomainOptional<Patient> findById(DomainId id);

    DomainOptional<Patient> findByUserId(DomainId id);

    DomainOptional<Patient> findByDocumentNumber(String number);

    DomainList<Patient> findAll(int page, int size);

    boolean existsByDocumentNumber(String number);

    boolean existsByEmail(String email);
}
