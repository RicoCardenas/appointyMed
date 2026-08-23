package com.appointymed.appointment.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookAppointmentRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        UUID specialtyId,

        @NotNull(message = "Appointment date and time is required")
        @Future(message = "Appointment date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime appointmentDateTime,

        @NotBlank(message = "Consultation reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason,

        String notes
) {}
