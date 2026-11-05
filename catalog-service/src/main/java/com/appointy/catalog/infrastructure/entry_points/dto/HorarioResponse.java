package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.HorarioAtencion;

public record HorarioResponse(
        String id, String profesionalId, String centroId, int diaSemana, String horaInicio, String horaFin, int duracionFranjaMin, boolean activo) {

    public static HorarioResponse de(HorarioAtencion h) {
        return new HorarioResponse(
                h.id(), h.profesionalId(), h.centroId(), h.diaSemana(), Horas.aTexto(h.horaInicio()), Horas.aTexto(h.horaFin()), h.duracionFranjaMin(), h.activo());
    }
}
