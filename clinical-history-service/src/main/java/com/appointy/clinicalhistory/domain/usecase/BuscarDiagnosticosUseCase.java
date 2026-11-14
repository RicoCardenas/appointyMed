package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.Diagnostico;

/** Busca en el catalogo CIE-10 (API externa). Solo PROFESIONAL; no se registra el termino. */
public interface BuscarDiagnosticosUseCase {

    Diagnostico[] ejecutar(Actor actor, String termino);
}
