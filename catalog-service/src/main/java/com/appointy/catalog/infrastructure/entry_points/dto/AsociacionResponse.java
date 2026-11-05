package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;

public record AsociacionResponse(boolean profesionalActivo, boolean asociado) {

    public static AsociacionResponse de(ValidarAsociacionPort.Resultado r) {
        return new AsociacionResponse(r.profesionalActivo(), r.asociado());
    }
}
