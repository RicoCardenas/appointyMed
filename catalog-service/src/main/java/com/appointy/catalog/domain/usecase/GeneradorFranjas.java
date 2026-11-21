package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.rule.FechaIso;

/** Calcula las franjas de un horario entre dos fechas ISO (inclusive). */
public final class GeneradorFranjas {

    private GeneradorFranjas() {}

    public static FranjaSolicitada[] generar(HorarioAtencion horario, String desde, String hasta) {
        int adelanto = (horario.diaSemana() - FechaIso.diaDeSemana(desde) + 7) % 7;
        long primero = FechaIso.diasDesdeEpoca(desde) + adelanto;
        long ultimo = FechaIso.diasDesdeEpoca(hasta);
        int semanas = primero > ultimo ? 0 : (int) ((ultimo - primero) / 7) + 1;
        int porDia = Math.max(0, (horario.horaFin() - horario.horaInicio()) / horario.duracionFranjaMin());
        FranjaSolicitada[] franjas = new FranjaSolicitada[semanas * porDia];
        int n = 0;
        for (int semana = 0; semana < semanas; semana++) {
            String fecha = FechaIso.deDiasDesdeEpoca(primero + 7L * semana);
            for (int k = 0; k < porDia; k++) {
                int inicio = horario.horaInicio() + k * horario.duracionFranjaMin();
                franjas[n++] = new FranjaSolicitada(fecha, inicio, inicio + horario.duracionFranjaMin());
            }
        }
        return franjas;
    }
}
