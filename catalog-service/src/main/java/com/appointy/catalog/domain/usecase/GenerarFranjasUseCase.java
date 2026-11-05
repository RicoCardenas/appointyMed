package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.ResultadoHorario;

/** CU-13/CU-25: reintenta la generacion de franjas de un horario (idempotente). */
public interface GenerarFranjasUseCase {

    ResultadoHorario ejecutar(String horarioId);
}
