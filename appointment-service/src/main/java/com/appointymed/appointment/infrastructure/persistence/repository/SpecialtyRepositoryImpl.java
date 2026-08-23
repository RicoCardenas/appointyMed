package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.SpecialtyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SpecialtyRepositoryImpl implements SpecialtyRepository {
    private final SpringDataSpecialtyRepository springDataRepository;

    public SpecialtyRepositoryImpl(SpringDataSpecialtyRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Specialty save(Specialty specialty) {
        SpecialtyJpaEntity entity = SpecialtyJpaEntity.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .consultationDurationMinutes(specialty.getConsultationDurationMinutes())
                .active(specialty.isActive())
                .createdAt(specialty.getCreatedAt())
                .updatedAt(specialty.getUpdatedAt())
                .build();
        SpecialtyJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Specialty> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Specialty> findByName(String name) {
        return springDataRepository.findByNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public List<Specialty> findAllActive() {
        return springDataRepository.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Specialty> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataRepository.existsByNameIgnoreCase(name);
    }

    private Specialty toDomain(SpecialtyJpaEntity entity) {
        return Specialty.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .consultationDurationMinutes(entity.getConsultationDurationMinutes())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
