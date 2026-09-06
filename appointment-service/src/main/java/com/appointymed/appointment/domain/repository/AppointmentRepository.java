package com.appointymed.appointment.domain.repository;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainOptional;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);

    DomainOptional<Appointment> findById(DomainId id);

    DomainList<Appointment> findByPatientId(DomainId id);

    DomainList<Appointment> findByPatientIdAndStatus(DomainId id, AppointmentStatus status);

    DomainList<Appointment> findByDoctorId(DomainId id);

    DomainList<Appointment> findByDoctorIdAndDateRange(
            DomainId id, DomainDateTime start, DomainDateTime end);

    DomainList<Appointment> findActiveAppointmentsByDoctorAndDate(
            DomainId id, DomainDateTime start, DomainDateTime end);

    boolean hasActiveAppointmentConflict(
            DomainId doctorId, DomainDateTime start, DomainDateTime end, DomainId excludeId);
}
