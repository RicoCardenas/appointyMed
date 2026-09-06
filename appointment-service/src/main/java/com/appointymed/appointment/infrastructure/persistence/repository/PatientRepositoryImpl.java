package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.PatientRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.PatientJpaEntity;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientRepositoryImpl implements PatientRepository {
    private final SpringDataPatientRepository springDataRepository;
    public PatientRepositoryImpl(SpringDataPatientRepository springDataRepository) { this.springDataRepository = springDataRepository; }
    @Override public Patient save(Patient patient) {
        PatientJpaEntity entity = PatientJpaEntity.builder().id(AppointmentDomainValueMapper.toUuid(patient.getId())).userId(AppointmentDomainValueMapper.toUuid(patient.getUserId())).documentType(patient.getDocumentType().name()).documentNumber(patient.getDocumentNumber()).firstName(patient.getFirstName()).lastName(patient.getLastName()).email(patient.getEmail()).phone(patient.getPhone()).birthDate(AppointmentDomainValueMapper.toLocalDate(patient.getBirthDate())).bloodType(patient.getBloodType()).address(patient.getAddress()).emergencyContactName(patient.getEmergencyContactName()).emergencyContactPhone(patient.getEmergencyContactPhone()).createdAt(AppointmentDomainValueMapper.toInstant(patient.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toInstant(patient.getUpdatedAt())).build();
        return toDomain(springDataRepository.save(entity));
    }
    @Override public DomainOptional<Patient> findById(DomainId id) { var value = springDataRepository.findById(AppointmentDomainValueMapper.toUuid(id)); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainOptional<Patient> findByUserId(DomainId id) { var value = springDataRepository.findByUserId(AppointmentDomainValueMapper.toUuid(id)); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainOptional<Patient> findByDocumentNumber(String number) { var value = springDataRepository.findByDocumentNumber(number); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainList<Patient> findAll(int page, int size) { DomainList<Patient> result = new DomainList<>(); for (PatientJpaEntity entity : springDataRepository.findAll(PageRequest.of(page, size))) result.add(toDomain(entity)); return result; }
    @Override public boolean existsByDocumentNumber(String number) { return springDataRepository.existsByDocumentNumber(number); }
    @Override public boolean existsByEmail(String email) { return springDataRepository.existsByEmailIgnoreCase(email); }
    private Patient toDomain(PatientJpaEntity e) { DocumentType type; try { type = DocumentType.valueOf(e.getDocumentType()); } catch (Exception ignored) { type = DocumentType.OTHER; } return Patient.builder().id(AppointmentDomainValueMapper.toDomainId(e.getId())).userId(AppointmentDomainValueMapper.toDomainId(e.getUserId())).documentType(type).documentNumber(e.getDocumentNumber()).firstName(e.getFirstName()).lastName(e.getLastName()).email(e.getEmail()).phone(e.getPhone()).birthDate(AppointmentDomainValueMapper.toDomainDate(e.getBirthDate())).bloodType(e.getBloodType()).address(e.getAddress()).emergencyContactName(e.getEmergencyContactName()).emergencyContactPhone(e.getEmergencyContactPhone()).createdAt(AppointmentDomainValueMapper.toDomainInstant(e.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toDomainInstant(e.getUpdatedAt())).build(); }
}
