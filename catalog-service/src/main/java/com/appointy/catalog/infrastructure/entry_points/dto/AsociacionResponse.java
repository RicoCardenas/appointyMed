package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.usecase.ProfesionalUseCase;

public record AsociacionResponse(boolean profesionalActivo, boolean asociado) {

    public static AsociacionResponse de(ProfesionalUseCase.ValidarAsociacionResultado r) {
        return new AsociacionResponse(r.profesionalActivo(), r.asociado());
    }
}
