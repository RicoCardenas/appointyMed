package com.appointymed.appointment.application.mapper;

import com.appointymed.appointment.application.dto.SpecialtyResponse;
import com.appointymed.appointment.domain.model.Specialty;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyMapper {
    public SpecialtyResponse toResponse(Specialty domain) {
        if (domain == null) {
            return null;
        }
        return new SpecialtyResponse(
                DomainValueMapper.toUuid(domain.getId()),
                domain.getName(),
                domain.getDescription(),
                domain.getConsultationDurationMinutes(),
                domain.isActive(),
                DomainValueMapper.toInstant(domain.getCreatedAt())
        );
    }
}
