package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.SpecialtyJpaEntity;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpecialtyRepositoryImpl implements SpecialtyRepository {
    private final SpringDataSpecialtyRepository springDataRepository;
    public SpecialtyRepositoryImpl(SpringDataSpecialtyRepository springDataRepository) { this.springDataRepository = springDataRepository; }
    @Override public Specialty save(Specialty specialty) { SpecialtyJpaEntity entity = SpecialtyJpaEntity.builder().id(AppointmentDomainValueMapper.toUuid(specialty.getId())).name(specialty.getName()).description(specialty.getDescription()).consultationDurationMinutes(specialty.getConsultationDurationMinutes()).active(specialty.isActive()).createdAt(AppointmentDomainValueMapper.toInstant(specialty.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toInstant(specialty.getUpdatedAt())).build(); return toDomain(springDataRepository.save(entity)); }
    @Override public DomainOptional<Specialty> findById(DomainId id) { var value = springDataRepository.findById(AppointmentDomainValueMapper.toUuid(id)); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainOptional<Specialty> findByName(String name) { var value = springDataRepository.findByNameIgnoreCase(name); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainList<Specialty> findAllActive() { return list(springDataRepository.findByActiveTrue()); }
    @Override public DomainList<Specialty> findAll() { return list(springDataRepository.findAll()); }
    @Override public boolean existsByName(String name) { return springDataRepository.existsByNameIgnoreCase(name); }
    private DomainList<Specialty> list(List<SpecialtyJpaEntity> entities) { DomainList<Specialty> result = new DomainList<>(); for (SpecialtyJpaEntity entity : entities) result.add(toDomain(entity)); return result; }
    private Specialty toDomain(SpecialtyJpaEntity e) { return Specialty.builder().id(AppointmentDomainValueMapper.toDomainId(e.getId())).name(e.getName()).description(e.getDescription()).consultationDurationMinutes(e.getConsultationDurationMinutes()).active(e.isActive()).createdAt(AppointmentDomainValueMapper.toDomainInstant(e.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toDomainInstant(e.getUpdatedAt())).build(); }
}
