package com.appointy.catalog.domain.rule;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.HorarioAtencion;

public final class ReglasHorario {

    public static final int MAX_MINUTO_DEL_DIA = 1439;

    private ReglasHorario() {}

    /** Dia 1 a 7, horas dentro del dia, fin posterior al inicio y duracion que divide el rango exacto. */
    public static void validar(int diaSemana, int horaInicio, int horaFin, int duracionFranjaMin) {
        if (diaSemana < 1 || diaSemana > 7) {
            throw new ValidacionException("DIA_INVALIDO", "El dia de la semana va de 1 (lunes) a 7 (domingo)");
        }
        if (horaInicio < 0 || horaFin > MAX_MINUTO_DEL_DIA || horaFin <= horaInicio) {
            throw new ValidacionException("HORAS_INVALIDAS", "La hora de fin debe ser posterior a la de inicio y ambas dentro del dia (hasta 23:59)");
        }
        if (duracionFranjaMin <= 0) {
            throw new ValidacionException("DURACION_INVALIDA", "La duracion de la franja debe ser positiva");
        }
        if ((horaFin - horaInicio) % duracionFranjaMin != 0) {
            throw new ValidacionException("DURACION_NO_DIVIDE_RANGO", "La duracion de la franja debe dividir exactamente el rango de atencion");
        }
    }

    /** Un profesional no puede estar en dos sitios a la vez: ningun horario activo del mismo dia puede cruzarse (en cualquier centro). */
    public static void validarSinTraslape(HorarioAtencion candidato, HorarioAtencion[] existentes) {
        for (HorarioAtencion otro : existentes) {
            if (!otro.activo() || otro.id().equals(candidato.id()) || otro.diaSemana() != candidato.diaSemana()) {
                continue;
            }
            if (candidato.horaInicio() < otro.horaFin() && otro.horaInicio() < candidato.horaFin()) {
                throw new ConflictoException("HORARIO_TRASLAPADO", "El horario se cruza con otro horario activo del profesional ese dia");
            }
        }
    }
}
