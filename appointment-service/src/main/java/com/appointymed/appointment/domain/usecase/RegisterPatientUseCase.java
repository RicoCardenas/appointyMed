package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.model.DocumentType;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;

public final class RegisterPatientUseCase {
    private final PatientRepository repository;

    public RegisterPatientUseCase(PatientRepository r) {
        repository = r;
    }

    public Patient execute(RegisterPatientCommand c) {
        if (repository.existsByDocumentNumber(c.documentNumber()))
            throw new DomainException("Document number is already registered", "PATIENT_DOCUMENT_EXISTS");
        if (repository.existsByEmail(c.email()))
            throw new DomainException("Email is already registered", "PATIENT_EMAIL_EXISTS");
        return repository.save(
                Patient.builder()
                        .userId(c.userId())
                        .documentType(c.documentType() == null ? DocumentType.CC : c.documentType())
                        .documentNumber(c.documentNumber())
                        .firstName(c.firstName())
                        .lastName(c.lastName())
                        .email(c.email())
                        .phone(c.phone())
                        .birthDate(c.birthDate())
                        .bloodType(c.bloodType())
                        .address(c.address())
                        .emergencyContactName(c.emergencyContactName())
                        .emergencyContactPhone(c.emergencyContactPhone())
                        .build());
    }

    public record RegisterPatientCommand(
            DomainId userId,
            DocumentType documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            com.appointymed.appointment.domain.model.DomainDate birthDate,
            String bloodType,
            String address,
            String emergencyContactName,
            String emergencyContactPhone) {}
}
