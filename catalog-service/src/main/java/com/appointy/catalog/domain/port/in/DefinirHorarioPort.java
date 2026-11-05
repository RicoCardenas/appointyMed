package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.ResultadoHorario;

/** CU-13: define un horario y pide a appointment generar las franjas. Sin duracion se usa el parametro duracion_franja_min. */
public interface DefinirHorarioPort {

    ResultadoHorario ejecutar(Comando comando);

    record Comando(String profesionalId, String centroId, int diaSemana, int horaInicio, int horaFin, Integer duracionFranjaMin) {}
}
