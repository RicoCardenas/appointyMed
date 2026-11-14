package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** La entrada original de una atencion y todas sus correcciones, por fecha ascendente. Mismas reglas y auditoria que CU-29. */
public interface ListarVersionesEntradaUseCase {

    EntradaClinica[] ejecutar(Actor actor, String entradaId);
}
