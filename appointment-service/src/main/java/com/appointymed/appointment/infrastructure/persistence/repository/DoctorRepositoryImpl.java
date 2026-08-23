package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.DoctorJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DoctorRepositoryImpl implements DoctorRepository {
    private final SpringDataDoctorRepository springDataRepository;

    public DoctorRepositoryImpl(SpringDataDoctorRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Doctor save(Doctor doctor) {
        DoctorJpaEntity entity = DoctorJpaEntity.builder()
                .id(doctor.getId())
                .userId(doctor.getUserId())
                .medicalLicense(doctor.getMedicalLicense())
                .specialtyId(doctor.getSpecialtyId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .consultationRoom(doctor.getConsultationRoom())
                .active(doctor.isActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
        DoctorJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Doctor> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Doctor> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<Doctor> findByMedicalLicense(String medicalLicense) {
        return springDataRepository.findByMedicalLicense(medicalLicense).map(this::toDomain);
    }

    @Override
    public List<Doctor> findBySpecialtyId(UUID specialtyId) {
        return springDataRepository.findBySpecialtyIdAndActiveTrue(specialtyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Doctor> findAllActive() {
        return springDataRepository.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Doctor> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByMedicalLicense(String medicalLicense) {
        return springDataRepository.existsByMedicalLicense(medicalLicense);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return springDataRepository.existsByUserId(userId);
    }

    private Doctor toDomain(DoctorJpaEntity entity) {
        return Doctor.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .medicalLicense(entity.getMedicalLicense())
                .specialtyId(entity.getSpecialtyId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .consultationRoom(entity.getConsultationRoom())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
