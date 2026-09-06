package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.DomainException;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;

public final class RegisterDoctorUseCase {
    private final DoctorRepository doctors;
    private final SpecialtyRepository specialties;

    public RegisterDoctorUseCase(DoctorRepository d, SpecialtyRepository s) {
        doctors = d;
        specialties = s;
    }

    public Doctor execute(RegisterDoctorCommand c) {
        if (doctors.existsByMedicalLicense(c.medicalLicense()))
            throw new DomainException("Medical license is already registered", "DOCTOR_LICENSE_EXISTS");
        if (doctors.existsByUserId(c.userId()))
            throw new DomainException("User is already registered as a doctor", "DOCTOR_USER_EXISTS");
        Specialty specialty =
                specialties
                        .findById(c.specialtyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Specialty", c.specialtyId()));
        return doctors.save(
                Doctor.builder()
                        .userId(c.userId())
                        .medicalLicense(c.medicalLicense())
                        .specialtyId(specialty.getId())
                        .firstName(c.firstName())
                        .lastName(c.lastName())
                        .email(c.email())
                        .phone(c.phone())
                        .consultationRoom(c.consultationRoom())
                        .active(true)
                        .build());
    }

    public record RegisterDoctorCommand(
            DomainId userId,
            String medicalLicense,
            DomainId specialtyId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String consultationRoom) {}
}
