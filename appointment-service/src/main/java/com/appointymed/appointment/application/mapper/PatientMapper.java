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
                DomainValueMapper.toUuid(domain.getId()),
                DomainValueMapper.toUuid(domain.getUserId()),
                domain.getDocumentType(),
                domain.getDocumentNumber(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getPhone(),
                DomainValueMapper.toLocalDate(domain.getBirthDate()),
                domain.getBloodType(),
                domain.getAddress(),
                domain.getEmergencyContactName(),
                domain.getEmergencyContactPhone(),
                DomainValueMapper.toInstant(domain.getCreatedAt())
        );
    }
}
