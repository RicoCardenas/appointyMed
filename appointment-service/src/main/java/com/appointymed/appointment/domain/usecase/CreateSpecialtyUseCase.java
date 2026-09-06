package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;

public final class CreateSpecialtyUseCase {
    private final SpecialtyRepository repository;

    public CreateSpecialtyUseCase(SpecialtyRepository r) {
        repository = r;
    }

    public Specialty execute(CreateSpecialtyCommand c) {
        if (repository.existsByName(c.name()))
            throw new DomainException("Specialty name is already registered", "SPECIALTY_EXISTS");
        return repository.save(
                Specialty.builder()
                        .name(c.name())
                        .description(c.description())
                        .consultationDurationMinutes(c.consultationDurationMinutes())
                        .active(true)
                        .build());
    }

    public record CreateSpecialtyCommand(
            String name, String description, int consultationDurationMinutes) {}
}
