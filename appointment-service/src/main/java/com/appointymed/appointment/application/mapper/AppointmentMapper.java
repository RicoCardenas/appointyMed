package com.appointymed.appointment.application.mapper;

import com.appointymed.appointment.application.dto.AppointmentResponse;
import com.appointymed.appointment.domain.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentResponse toResponse(Appointment domain, String doctorName, String patientName, String patientPhone, String specialtyName) {
        if (domain == null) {
            return null;
        }
        return new AppointmentResponse(
                DomainValueMapper.toUuid(domain.getId()),
                DomainValueMapper.toUuid(domain.getDoctorId()),
                doctorName,
                DomainValueMapper.toUuid(domain.getPatientId()),
                patientName,
                patientPhone,
                DomainValueMapper.toUuid(domain.getSpecialtyId()),
                specialtyName,
                DomainValueMapper.toLocalDateTime(domain.getAppointmentDateTime()),
                DomainValueMapper.toLocalDateTime(domain.getEndDateTime()),
                domain.getStatus(),
                domain.getReason(),
                domain.getCancellationReason(),
                domain.getNotes(),
                DomainValueMapper.toInstant(domain.getCreatedAt())
        );
    }
}
