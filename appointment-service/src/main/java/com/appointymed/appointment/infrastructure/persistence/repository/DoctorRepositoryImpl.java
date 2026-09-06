package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.infrastructure.persistence.entity.DoctorJpaEntity;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorRepositoryImpl implements DoctorRepository {
    private final SpringDataDoctorRepository springDataRepository;
    public DoctorRepositoryImpl(SpringDataDoctorRepository springDataRepository) { this.springDataRepository = springDataRepository; }
    @Override public Doctor save(Doctor doctor) {
        DoctorJpaEntity entity = DoctorJpaEntity.builder().id(AppointmentDomainValueMapper.toUuid(doctor.getId())).userId(AppointmentDomainValueMapper.toUuid(doctor.getUserId())).medicalLicense(doctor.getMedicalLicense()).specialtyId(AppointmentDomainValueMapper.toUuid(doctor.getSpecialtyId())).firstName(doctor.getFirstName()).lastName(doctor.getLastName()).email(doctor.getEmail()).phone(doctor.getPhone()).consultationRoom(doctor.getConsultationRoom()).active(doctor.isActive()).createdAt(AppointmentDomainValueMapper.toInstant(doctor.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toInstant(doctor.getUpdatedAt())).build();
        return toDomain(springDataRepository.save(entity));
    }
    @Override public DomainOptional<Doctor> findById(DomainId id) { var value = springDataRepository.findById(AppointmentDomainValueMapper.toUuid(id)); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainOptional<Doctor> findByUserId(DomainId id) { var value = springDataRepository.findByUserId(AppointmentDomainValueMapper.toUuid(id)); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainOptional<Doctor> findByMedicalLicense(String license) { var value = springDataRepository.findByMedicalLicense(license); return value.isPresent() ? DomainOptional.of(toDomain(value.get())) : DomainOptional.empty(); }
    @Override public DomainList<Doctor> findBySpecialtyId(DomainId id) { return list(springDataRepository.findBySpecialtyIdAndActiveTrue(AppointmentDomainValueMapper.toUuid(id))); }
    @Override public DomainList<Doctor> findAllActive() { return list(springDataRepository.findByActiveTrue()); }
    @Override public DomainList<Doctor> findAll() { return list(springDataRepository.findAll()); }
    @Override public boolean existsByMedicalLicense(String license) { return springDataRepository.existsByMedicalLicense(license); }
    @Override public boolean existsByUserId(DomainId id) { return springDataRepository.existsByUserId(AppointmentDomainValueMapper.toUuid(id)); }
    private DomainList<Doctor> list(List<DoctorJpaEntity> entities) { DomainList<Doctor> result = new DomainList<>(); for (DoctorJpaEntity entity : entities) result.add(toDomain(entity)); return result; }
    private Doctor toDomain(DoctorJpaEntity entity) { return Doctor.builder().id(AppointmentDomainValueMapper.toDomainId(entity.getId())).userId(AppointmentDomainValueMapper.toDomainId(entity.getUserId())).medicalLicense(entity.getMedicalLicense()).specialtyId(AppointmentDomainValueMapper.toDomainId(entity.getSpecialtyId())).firstName(entity.getFirstName()).lastName(entity.getLastName()).email(entity.getEmail()).phone(entity.getPhone()).consultationRoom(entity.getConsultationRoom()).active(entity.isActive()).createdAt(AppointmentDomainValueMapper.toDomainInstant(entity.getCreatedAt())).updatedAt(AppointmentDomainValueMapper.toDomainInstant(entity.getUpdatedAt())).build(); }
}
