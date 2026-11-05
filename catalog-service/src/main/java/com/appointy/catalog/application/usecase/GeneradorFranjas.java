package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Calcula las franjas de un horario entre dos fechas; el calendario se resuelve aqui, fuera del dominio. */
public final class GeneradorFranjas {

    private GeneradorFranjas() {}

    public static FranjaSolicitada[] generar(HorarioAtencion horario, LocalDate desde, LocalDate hasta) {
        List<FranjaSolicitada> franjas = new ArrayList<>();
        int adelanto = (horario.diaSemana() - desde.getDayOfWeek().getValue() + 7) % 7;
        for (LocalDate fecha = desde.plusDays(adelanto); !fecha.isAfter(hasta); fecha = fecha.plusDays(7)) {
            for (int inicio = horario.horaInicio();
                    inicio + horario.duracionFranjaMin() <= horario.horaFin();
                    inicio += horario.duracionFranjaMin()) {
                franjas.add(new FranjaSolicitada(fecha.toString(), inicio, inicio + horario.duracionFranjaMin()));
            }
        }
        return franjas.toArray(new FranjaSolicitada[0]);
    }
}
