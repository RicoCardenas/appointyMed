package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.repository.DoctorRepository;

public final class GetAllDoctorsUseCase {
    private final DoctorRepository repository;

    public GetAllDoctorsUseCase(DoctorRepository r) {
        repository = r;
    }

    public DomainList<Doctor> execute(DomainId specialtyId) {
        return specialtyId == null
                ? repository.findAllActive()
                : repository.findBySpecialtyId(specialtyId);
    }
}
