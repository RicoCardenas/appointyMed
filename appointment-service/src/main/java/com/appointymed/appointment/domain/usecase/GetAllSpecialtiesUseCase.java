package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;

public final class GetAllSpecialtiesUseCase {
    private final SpecialtyRepository repository;

    public GetAllSpecialtiesUseCase(SpecialtyRepository r) {
        repository = r;
    }

    public DomainList<Specialty> execute() {
        return repository.findAllActive();
    }
}
