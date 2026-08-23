package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.DocumentType;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.repository.PatientRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.PatientJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PatientRepositoryImpl implements PatientRepository {
    private final SpringDataPatientRepository springDataRepository;

    public PatientRepositoryImpl(SpringDataPatientRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Patient save(Patient patient) {
        PatientJpaEntity entity = PatientJpaEntity.builder()
                .id(patient.getId())
                .userId(patient.getUserId())
                .documentType(patient.getDocumentType().name())
                .documentNumber(patient.getDocumentNumber())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .birthDate(patient.getBirthDate())
                .bloodType(patient.getBloodType())
                .address(patient.getAddress())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
        PatientJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Patient> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<Patient> findByDocumentNumber(String documentNumber) {
        return springDataRepository.findByDocumentNumber(documentNumber).map(this::toDomain);
    }

    @Override
    public List<Patient> findAll(int page, int size) {
        return springDataRepository.findAll(PageRequest.of(page, size)).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return springDataRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmailIgnoreCase(email);
    }

    private Patient toDomain(PatientJpaEntity e) {
        DocumentType docType;
        try {
            docType = DocumentType.valueOf(e.getDocumentType());
        } catch (Exception ex) {
            docType = DocumentType.OTHER;
        }

        return Patient.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .documentType(docType)
                .documentNumber(e.getDocumentNumber())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .birthDate(e.getBirthDate())
                .bloodType(e.getBloodType())
                .address(e.getAddress())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
