package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.Franja;

public record FranjaResponse(String id, String profesionalId, String centroId, String fecha, String horaInicio, String horaFin, String estado) {

    public static FranjaResponse de(Franja f) {
        return new FranjaResponse(f.id(), f.profesionalId(), f.centroId(), f.fecha(), Horas.aTexto(f.horaInicio()), Horas.aTexto(f.horaFin()),
                f.estado().name());
    }
}
