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

        List<ScheduleResponse> scheduleResponses = new java.util.ArrayList<>();
        if (domain.getSchedules() != null) {
            for (int index = 0; index < domain.getSchedules().size(); index++) scheduleResponses.add(toScheduleResponse(domain.getSchedules().get(index)));
        }

        return new DoctorResponse(
                DomainValueMapper.toUuid(domain.getId()),
                DomainValueMapper.toUuid(domain.getUserId()),
                domain.getMedicalLicense(),
                DomainValueMapper.toUuid(domain.getSpecialtyId()),
                specialtyName,
                domain.getFirstName(),
                domain.getLastName(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getConsultationRoom(),
                domain.isActive(),
                scheduleResponses,
                DomainValueMapper.toInstant(domain.getCreatedAt())
        );
    }

    public ScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return new ScheduleResponse(
                DomainValueMapper.toUuid(schedule.getId()),
                DomainValueMapper.toUuid(schedule.getDoctorId()),
                schedule.getDayOfWeek(),
                ScheduleResponse.getDayNameFromInt(schedule.getDayOfWeek()),
                DomainValueMapper.toLocalTime(schedule.getStartTime()),
                DomainValueMapper.toLocalTime(schedule.getEndTime()),
                schedule.getSlotDurationMinutes(),
                schedule.isActive()
        );
    }
}
