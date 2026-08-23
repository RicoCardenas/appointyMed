package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class UpdatePatientUseCase {
    private final PatientRepository patientRepository;

    public UpdatePatientUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient execute(UUID patientId, UpdatePatientCommand command) {
        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        Patient updated = Patient.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .documentType(existing.getDocumentType())
                .documentNumber(existing.getDocumentNumber())
                .firstName(command.firstName() != null ? command.firstName() : existing.getFirstName())
                .lastName(command.lastName() != null ? command.lastName() : existing.getLastName())
                .email(command.email() != null ? command.email() : existing.getEmail())
                .phone(command.phone() != null ? command.phone() : existing.getPhone())
                .birthDate(command.birthDate() != null ? command.birthDate() : existing.getBirthDate())
                .bloodType(command.bloodType() != null ? command.bloodType() : existing.getBloodType())
                .address(command.address() != null ? command.address() : existing.getAddress())
                .emergencyContactName(command.emergencyContactName() != null ? command.emergencyContactName() : existing.getEmergencyContactName())
                .emergencyContactPhone(command.emergencyContactPhone() != null ? command.emergencyContactPhone() : existing.getEmergencyContactPhone())
                .createdAt(existing.getCreatedAt())
                .build();

        return patientRepository.save(updated);
    }

    public record UpdatePatientCommand(
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
