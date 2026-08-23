package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterDoctorUseCase {
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    public RegisterDoctorUseCase(DoctorRepository doctorRepository, SpecialtyRepository specialtyRepository) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
    }

    public Doctor execute(RegisterDoctorCommand command) {
        if (doctorRepository.existsByMedicalLicense(command.medicalLicense())) {
            throw new DomainException("Medical license '" + command.medicalLicense() + "' is already registered", "DOCTOR_LICENSE_EXISTS");
        }
        if (doctorRepository.existsByUserId(command.userId())) {
            throw new DomainException("User ID '" + command.userId() + "' is already registered as a doctor", "DOCTOR_USER_EXISTS");
        }

        Specialty specialty = specialtyRepository.findById(command.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", command.specialtyId()));

        Doctor doctor = Doctor.builder()
                .id(UUID.randomUUID())
                .userId(command.userId())
                .medicalLicense(command.medicalLicense())
                .specialtyId(specialty.getId())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .phone(command.phone())
                .consultationRoom(command.consultationRoom())
                .active(true)
                .build();

        return doctorRepository.save(doctor);
    }

    public record RegisterDoctorCommand(
            UUID userId,
            String medicalLicense,
            UUID specialtyId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String consultationRoom
    ) {}
}
