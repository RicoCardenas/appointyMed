package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.ResultadoHorario;

/** CU-14: modifica un horario y pide generar las franjas nuevas. */
public interface ModificarHorarioPort {

    ResultadoHorario ejecutar(Comando comando);

    record Comando(String horarioId, String centroId, int diaSemana, int horaInicio, int horaFin, Integer duracionFranjaMin, boolean activo) {}
}
