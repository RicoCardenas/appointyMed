package com.appointy.appointment.domain.model;

/** Instantes en milisegundos desde 1970 (UTC). {@code version} es null mientras la cita no se ha guardado. */
public record Cita(
        String id,
        String pacienteId,
        String profesionalId,
        String especialidadId,
        String centroId,
        String franjaId,
        EstadoCita estado,
        long creadoEn,
        long modificadoEn,
        Long version) {

    public Cita conEstado(EstadoCita nuevo, long ahora) {
        return new Cita(id, pacienteId, profesionalId, especialidadId, centroId, franjaId, nuevo, creadoEn, ahora, version);
    }

    public Cita reprogramada(Franja nueva, long ahora) {
        return new Cita(id, pacienteId, nueva.profesionalId(), especialidadId, nueva.centroId(), nueva.id(), estado, creadoEn, ahora, version);
    }
}
