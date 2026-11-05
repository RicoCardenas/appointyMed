package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.ResultadoHorario;

public record ResultadoHorarioResponse(HorarioResponse horario, boolean franjasGeneradas, int franjasCreadas, int franjasExistentes, String mensaje) {

    public static ResultadoHorarioResponse de(ResultadoHorario r) {
        return new ResultadoHorarioResponse(HorarioResponse.de(r.horario()), r.franjasGeneradas(), r.franjasCreadas(), r.franjasExistentes(), r.mensaje());
    }
}
