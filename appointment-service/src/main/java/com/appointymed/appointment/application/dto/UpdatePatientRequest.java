package com.appointymed.appointment.application.dto;

import java.time.LocalDate;

public record UpdatePatientRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        String bloodType,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {}
