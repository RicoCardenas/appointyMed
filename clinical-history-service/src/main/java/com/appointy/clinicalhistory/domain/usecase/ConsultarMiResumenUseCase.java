package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;

/** Resumen de la propia historia del paciente (mismas reglas y auditoria que CU-30). */
public interface ConsultarMiResumenUseCase {

    ResumenHistoria ejecutar(Actor actor);
}
