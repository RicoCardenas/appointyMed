package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;

public final class GetPatientByIdUseCase {
    private final PatientRepository repository;

    public GetPatientByIdUseCase(PatientRepository r) {
        repository = r;
    }

    public Patient execute(DomainId id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public Patient executeByUserId(DomainId id) {
        return repository
                .findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with User ID", id));
    }

    public DomainList<Patient> executeAll(int page, int size) {
        return repository.findAll(page, size);
    }
}
