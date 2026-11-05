package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.ResultadoHorario;

/** CU-13/CU-25: reintenta la generacion de franjas de un horario (idempotente). */
public interface GenerarFranjasPort {

    ResultadoHorario ejecutar(String horarioId);
}
