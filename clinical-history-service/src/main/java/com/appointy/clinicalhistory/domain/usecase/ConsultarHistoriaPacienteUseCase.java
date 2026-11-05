package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** CU-29: un profesional con relacion vigente o pasada con el paciente (CU-27) lee su historia completa. */
public interface ConsultarHistoriaPacienteUseCase {

    EntradaClinica[] ejecutar(Actor actor, String pacienteId);
}
