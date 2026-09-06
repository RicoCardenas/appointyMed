package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.DomainDate;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.repository.AppointmentRepository;

public final class GetDoctorAppointmentsUseCase {
    private final AppointmentRepository repository;

    public GetDoctorAppointmentsUseCase(AppointmentRepository r) {
        repository = r;
    }

    public DomainList<Appointment> execute(DomainId id, DomainDate date) {
        if (date == null) {
            return repository.findByDoctorId(id);
        }
        return repository.findByDoctorIdAndDateRange(
                id,
                date.atStartOfDay(),
                date.atTime(com.appointymed.appointment.domain.model.DomainTime.MAX));
    }
}
