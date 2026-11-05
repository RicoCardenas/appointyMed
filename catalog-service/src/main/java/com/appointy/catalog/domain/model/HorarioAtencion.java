package com.appointy.catalog.domain.model;

/**
 * Horario semanal de un profesional en un centro. {@code diaSemana}: 1 lunes a 7 domingo.
 * Las horas son minutos desde medianoche (de 0 a 1439) y la duracion de la franja esta en minutos.
 */
public record HorarioAtencion(
        String id, String profesionalId, String centroId, int diaSemana, int horaInicio, int horaFin, int duracionFranjaMin, boolean activo) {

    public HorarioAtencion modificar(String nuevoCentroId, int nuevoDia, int nuevaHoraInicio, int nuevaHoraFin, int nuevaDuracion, boolean nuevoActivo) {
        return new HorarioAtencion(id, profesionalId, nuevoCentroId, nuevoDia, nuevaHoraInicio, nuevaHoraFin, nuevaDuracion, nuevoActivo);
    }
}
