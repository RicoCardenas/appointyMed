package com.appointymed.appointment.application.mapper;

import com.appointymed.appointment.application.dto.DoctorResponse;
import com.appointymed.appointment.application.dto.ScheduleResponse;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor domain, String specialtyName) {
        if (domain == null) {
            return null;
        }

        List<ScheduleResponse> scheduleResponses = domain.getSchedules() != null ?
                domain.getSchedules().stream().map(this::toScheduleResponse).toList() :
                Collections.emptyList();

        return new DoctorResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getMedicalLicense(),
                domain.getSpecialtyId(),
                specialtyName,
                domain.getFirstName(),
                domain.getLastName(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getConsultationRoom(),
                domain.isActive(),
                scheduleResponses,
                domain.getCreatedAt()
        );
    }

    public ScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getDoctorId(),
                schedule.getDayOfWeek(),
                ScheduleResponse.getDayNameFromInt(schedule.getDayOfWeek()),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDurationMinutes(),
                schedule.isActive()
        );
    }
}
