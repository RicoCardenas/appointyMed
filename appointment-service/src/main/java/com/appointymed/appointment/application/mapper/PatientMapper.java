package com.appointymed.appointment.application.mapper;

import com.appointymed.appointment.application.dto.PatientResponse;
import com.appointymed.appointment.domain.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientResponse toResponse(Patient domain) {
        if (domain == null) {
            return null;
        }
        return new PatientResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getDocumentType(),
                domain.getDocumentNumber(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getBirthDate(),
                domain.getBloodType(),
                domain.getAddress(),
                domain.getEmergencyContactName(),
                domain.getEmergencyContactPhone(),
                domain.getCreatedAt()
        );
    }
}
