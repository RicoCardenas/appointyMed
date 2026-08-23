package com.appointymed.appointment.application.service;

import com.appointymed.appointment.application.dto.CreateSpecialtyRequest;
import com.appointymed.appointment.application.dto.SpecialtyResponse;
import com.appointymed.appointment.application.mapper.SpecialtyMapper;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import com.appointymed.appointment.domain.usecase.CreateSpecialtyUseCase;
import com.appointymed.appointment.domain.usecase.GetAllSpecialtiesUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SpecialtyApplicationService {
    private final CreateSpecialtyUseCase createSpecialtyUseCase;
    private final GetAllSpecialtiesUseCase getAllSpecialtiesUseCase;
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    public SpecialtyApplicationService(CreateSpecialtyUseCase createSpecialtyUseCase,
                                     GetAllSpecialtiesUseCase getAllSpecialtiesUseCase,
                                     SpecialtyRepository specialtyRepository,
                                     SpecialtyMapper specialtyMapper) {
        this.createSpecialtyUseCase = createSpecialtyUseCase;
        this.getAllSpecialtiesUseCase = getAllSpecialtiesUseCase;
        this.specialtyRepository = specialtyRepository;
        this.specialtyMapper = specialtyMapper;
    }

    public SpecialtyResponse createSpecialty(CreateSpecialtyRequest request) {
        var command = new CreateSpecialtyUseCase.CreateSpecialtyCommand(
                request.name(),
                request.description(),
                request.consultationDurationMinutes()
        );
        Specialty specialty = createSpecialtyUseCase.execute(command);
        return specialtyMapper.toResponse(specialty);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAllSpecialties() {
        return getAllSpecialtiesUseCase.execute().stream()
                .map(specialtyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse getSpecialtyById(UUID id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", id));
        return specialtyMapper.toResponse(specialty);
    }
}
