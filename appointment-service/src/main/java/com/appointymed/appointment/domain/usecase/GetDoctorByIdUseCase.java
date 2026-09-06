package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.repository.DoctorRepository;

public final class GetDoctorByIdUseCase {
    private final DoctorRepository repository;

    public GetDoctorByIdUseCase(DoctorRepository r) {
        repository = r;
    }

    public Doctor execute(DomainId id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    public Doctor executeByUserId(DomainId id) {
        return repository
                .findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with User ID", id));
    }
}
