package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GetAllDoctorsUseCase {
    private final DoctorRepository doctorRepository;

    public GetAllDoctorsUseCase(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> execute(UUID specialtyId) {
        if (specialtyId != null) {
            return doctorRepository.findBySpecialtyId(specialtyId);
        }
        return doctorRepository.findAllActive();
    }
}
