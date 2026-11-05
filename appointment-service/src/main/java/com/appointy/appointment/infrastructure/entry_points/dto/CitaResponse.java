package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.Franja;
import java.time.Instant;

public record CitaResponse(
        String id,
        String pacienteId,
        String profesionalId,
        String especialidadId,
        String centroId,
        String franjaId,
        String estado,
        String fecha,
        String horaInicio,
        String horaFin,
        Instant creadoEn,
        Instant modificadoEn) {

    public static CitaResponse de(CitaConFranja cf) {
        Cita c = cf.cita();
        Franja f = cf.franja();
        return new CitaResponse(c.id(), c.pacienteId(), c.profesionalId(), c.especialidadId(), c.centroId(), c.franjaId(), c.estado().name(),
                f == null ? null : f.fecha(), f == null ? null : Horas.aTexto(f.horaInicio()), f == null ? null : Horas.aTexto(f.horaFin()),
                Instant.ofEpochMilli(c.creadoEn()), Instant.ofEpochMilli(c.modificadoEn()));
    }
}
