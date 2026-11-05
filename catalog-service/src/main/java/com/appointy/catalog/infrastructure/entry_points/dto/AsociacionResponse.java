package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.usecase.ValidarAsociacionUseCase;

public record AsociacionResponse(boolean profesionalActivo, boolean asociado) {

    public static AsociacionResponse de(ValidarAsociacionUseCase.Resultado r) {
        return new AsociacionResponse(r.profesionalActivo(), r.asociado());
    }
}
