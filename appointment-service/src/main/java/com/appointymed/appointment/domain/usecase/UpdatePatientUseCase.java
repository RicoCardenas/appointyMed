package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.DomainDate;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;

public final class UpdatePatientUseCase {
    private final PatientRepository repository;

    public UpdatePatientUseCase(PatientRepository r) {
        repository = r;
    }

    public Patient execute(DomainId id, UpdatePatientCommand c) {
        Patient p =
                repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return repository.save(
                Patient.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .documentType(p.getDocumentType())
                        .documentNumber(p.getDocumentNumber())
                        .firstName(value(c.firstName(), p.getFirstName()))
                        .lastName(value(c.lastName(), p.getLastName()))
                        .email(value(c.email(), p.getEmail()))
                        .phone(value(c.phone(), p.getPhone()))
                        .birthDate(c.birthDate() == null ? p.getBirthDate() : c.birthDate())
                        .bloodType(value(c.bloodType(), p.getBloodType()))
                        .address(value(c.address(), p.getAddress()))
                        .emergencyContactName(value(c.emergencyContactName(), p.getEmergencyContactName()))
                        .emergencyContactPhone(value(c.emergencyContactPhone(), p.getEmergencyContactPhone()))
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build());
    }

    private String value(String incoming, String current) {
        return incoming == null ? current : incoming;
    }

    public record UpdatePatientCommand(
            String firstName,
            String lastName,
            String email,
            String phone,
            DomainDate birthDate,
            String bloodType,
            String address,
            String emergencyContactName,
            String emergencyContactPhone) {}
}
