package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllSpecialtiesUseCase {
    private final SpecialtyRepository specialtyRepository;

    public GetAllSpecialtiesUseCase(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public List<Specialty> execute() {
        return specialtyRepository.findAllActive();
    }
}
