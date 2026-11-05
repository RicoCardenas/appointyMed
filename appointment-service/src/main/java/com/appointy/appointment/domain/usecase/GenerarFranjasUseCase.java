package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.FranjaNueva;
import com.appointy.appointment.domain.model.ResultadoGeneracion;

/** CU-25: idempotente (RNF-16). */
public interface GenerarFranjasUseCase {

    ResultadoGeneracion ejecutar(String profesionalId, String centroId, FranjaNueva[] franjas);
}
