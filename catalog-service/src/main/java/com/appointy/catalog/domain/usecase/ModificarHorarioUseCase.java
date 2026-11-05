package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.ResultadoHorario;

/** CU-14: modifica un horario y pide generar las franjas nuevas. */
public interface ModificarHorarioUseCase {

    ResultadoHorario ejecutar(Comando comando);

    record Comando(String horarioId, String centroId, int diaSemana, int horaInicio, int horaFin, Integer duracionFranjaMin, boolean activo) {}
}
