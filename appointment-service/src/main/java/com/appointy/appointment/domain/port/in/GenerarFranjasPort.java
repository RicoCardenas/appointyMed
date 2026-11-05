package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.FranjaNueva;
import com.appointy.appointment.domain.model.ResultadoGeneracion;

/** CU-25: idempotente (RNF-16). */
public interface GenerarFranjasPort {

    ResultadoGeneracion ejecutar(String profesionalId, String centroId, FranjaNueva[] franjas);
}
