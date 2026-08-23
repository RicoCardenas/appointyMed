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
                domain.getId(),
                domain.getDoctorId(),
                doctorName,
                domain.getPatientId(),
                patientName,
                patientPhone,
                domain.getSpecialtyId(),
                specialtyName,
                domain.getAppointmentDateTime(),
                domain.getEndDateTime(),
                domain.getStatus(),
                domain.getReason(),
                domain.getCancellationReason(),
                domain.getNotes(),
                domain.getCreatedAt()
        );
    }
}
