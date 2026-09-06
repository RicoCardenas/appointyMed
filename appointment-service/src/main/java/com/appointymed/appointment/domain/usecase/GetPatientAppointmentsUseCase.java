package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.model.DomainClock;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.repository.AppointmentRepository;

public final class GetPatientAppointmentsUseCase {
    private final AppointmentRepository repository;
    private final DomainClock clock;

    public GetPatientAppointmentsUseCase(AppointmentRepository r, DomainClock c) {
        repository = r;
        clock = c;
    }

    public DomainList<Appointment> executeAll(DomainId id) {
        return repository.findByPatientId(id);
    }

    public DomainList<Appointment> executeUpcoming(DomainId id) {
        return filter(id, true);
    }

    public DomainList<Appointment> executeHistory(DomainId id) {
        return filter(id, false);
    }

    private DomainList<Appointment> filter(DomainId id, boolean upcoming) {
        DomainList<Appointment> source = repository.findByPatientId(id), result = new DomainList<>();
        DomainDateTime now = clock.now();
        for (int i = 0; i < source.size(); i++) {
            Appointment a = source.get(i);
            boolean include =
                    upcoming
                            ? a.getAppointmentDateTime().isAfter(now)
                                    && a.getStatus() != AppointmentStatus.CANCELLED
                            : a.getAppointmentDateTime().isBefore(now)
                                    || a.getStatus() == AppointmentStatus.COMPLETED
                                    || a.getStatus() == AppointmentStatus.CANCELLED;
            if (include) {
                result.add(a);
            }
        }
        return result;
    }
}
