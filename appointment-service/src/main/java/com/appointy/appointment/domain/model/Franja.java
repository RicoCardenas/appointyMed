package com.appointy.appointment.domain.model;

/**
 * {@code fecha} es texto ISO yyyy-MM-dd; las horas, minutos desde medianoche; ambas en la hora local de la clinica.
 * {@code version} es null mientras la franja no se ha guardado.
 */
public record Franja(
        String id, String profesionalId, String centroId, String fecha, int horaInicio, int horaFin, EstadoFranja estado, Long version) {

    public Franja ocupar() {
        return conEstado(EstadoFranja.OCUPADA);
    }

    public Franja liberar() {
        return conEstado(EstadoFranja.DISPONIBLE);
    }

    public Franja bloquear() {
        return conEstado(EstadoFranja.BLOQUEADA);
    }

    private Franja conEstado(EstadoFranja nuevo) {
        return new Franja(id, profesionalId, centroId, fecha, horaInicio, horaFin, nuevo, version);
    }
}
