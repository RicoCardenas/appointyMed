package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** CU-30: el paciente lee unicamente su propia historia. */
public interface ConsultarMiHistoriaUseCase {

    EntradaClinica[] ejecutar(Actor actor);
}
