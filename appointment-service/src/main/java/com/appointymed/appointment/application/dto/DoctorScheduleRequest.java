package com.appointymed.appointment.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DoctorScheduleRequest(
        @NotEmpty(message = "Schedule days cannot be empty")
        List<@Valid DayScheduleRequest> schedules
) {}
