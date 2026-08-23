package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateSpecialtyUseCase {
    private final SpecialtyRepository specialtyRepository;

    public CreateSpecialtyUseCase(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public Specialty execute(CreateSpecialtyCommand command) {
        if (specialtyRepository.existsByName(command.name())) {
            throw new DomainException("Specialty with name '" + command.name() + "' already exists", "SPECIALTY_ALREADY_EXISTS");
        }

        Specialty specialty = Specialty.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .description(command.description())
                .consultationDurationMinutes(command.consultationDurationMinutes())
                .active(true)
                .build();

        return specialtyRepository.save(specialty);
    }

    public record CreateSpecialtyCommand(
            String name,
            String description,
            int consultationDurationMinutes
    ) {}
}
