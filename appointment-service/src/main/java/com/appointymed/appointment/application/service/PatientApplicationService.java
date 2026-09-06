package com.appointymed.appointment.application.service;

import com.appointymed.appointment.application.dto.PatientResponse;
import com.appointymed.appointment.application.dto.RegisterPatientRequest;
import com.appointymed.appointment.application.dto.UpdatePatientRequest;
import com.appointymed.appointment.application.mapper.PatientMapper;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.usecase.GetPatientByIdUseCase;
import com.appointymed.appointment.domain.usecase.RegisterPatientUseCase;
import com.appointymed.appointment.domain.usecase.UpdatePatientUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.appointymed.appointment.application.mapper.DomainValueMapper;

@Service
@Transactional
public class PatientApplicationService {
    private final RegisterPatientUseCase registerPatientUseCase;
    private final GetPatientByIdUseCase getPatientByIdUseCase;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final PatientMapper patientMapper;

    public PatientApplicationService(RegisterPatientUseCase registerPatientUseCase,
                                   GetPatientByIdUseCase getPatientByIdUseCase,
                                   UpdatePatientUseCase updatePatientUseCase,
                                   PatientMapper patientMapper) {
        this.registerPatientUseCase = registerPatientUseCase;
        this.getPatientByIdUseCase = getPatientByIdUseCase;
        this.updatePatientUseCase = updatePatientUseCase;
        this.patientMapper = patientMapper;
    }

    public PatientResponse registerPatient(RegisterPatientRequest request) {
        var command = new RegisterPatientUseCase.RegisterPatientCommand(
                DomainValueMapper.toDomainId(request.userId()),
                request.documentType(),
                request.documentNumber(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                DomainValueMapper.toDomainDate(request.birthDate()),
                request.bloodType(),
                request.address(),
                request.emergencyContactName(),
                request.emergencyContactPhone()
        );
        Patient patient = registerPatientUseCase.execute(command);
        return patientMapper.toResponse(patient);
    }

    public PatientResponse updatePatient(UUID id, UpdatePatientRequest request) {
        var command = new UpdatePatientUseCase.UpdatePatientCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                DomainValueMapper.toDomainDate(request.birthDate()),
                request.bloodType(),
                request.address(),
                request.emergencyContactName(),
                request.emergencyContactPhone()
        );
        Patient patient = updatePatientUseCase.execute(DomainId.of(id.toString()), command);
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        Patient patient = getPatientByIdUseCase.execute(DomainId.of(id.toString()));
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientByUserId(UUID userId) {
        Patient patient = getPatientByIdUseCase.executeByUserId(DomainId.of(userId.toString()));
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(int page, int size) {
        DomainList<Patient> patients = getPatientByIdUseCase.executeAll(page, size);
        List<PatientResponse> result = new java.util.ArrayList<>();
        for (int index = 0; index < patients.size(); index++) result.add(patientMapper.toResponse(patients.get(index)));
        return result;
    }
}
