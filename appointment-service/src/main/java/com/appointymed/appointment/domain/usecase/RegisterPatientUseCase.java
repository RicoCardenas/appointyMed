package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.model.DocumentType;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class RegisterPatientUseCase {
    private final PatientRepository patientRepository;

    public RegisterPatientUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient execute(RegisterPatientCommand command) {
        if (patientRepository.existsByDocumentNumber(command.documentNumber())) {
            throw new DomainException("Document number '" + command.documentNumber() + "' is already registered", "PATIENT_DOCUMENT_EXISTS");
        }

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .userId(command.userId())
                .documentType(command.documentType() != null ? command.documentType() : DocumentType.CC)
                .documentNumber(command.documentNumber())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .phone(command.phone())
                .birthDate(command.birthDate())
                .bloodType(command.bloodType())
                .address(command.address())
                .emergencyContactName(command.emergencyContactName())
                .emergencyContactPhone(command.emergencyContactPhone())
                .build();

        return patientRepository.save(patient);
    }

    public record RegisterPatientCommand(
            UUID userId,
            DocumentType documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            LocalDate birthDate,
            String bloodType,
            String address,
            String emergencyContactName,
            String emergencyContactPhone
    ) {}
}
