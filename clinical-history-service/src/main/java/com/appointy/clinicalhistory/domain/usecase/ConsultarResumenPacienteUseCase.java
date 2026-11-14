package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;

/** Resumen de la historia de un paciente para un profesional con relacion (mismas reglas y auditoria que CU-29). */
public interface ConsultarResumenPacienteUseCase {

    ResumenHistoria ejecutar(Actor actor, String pacienteId);
}
