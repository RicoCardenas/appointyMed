package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;

/** Listado paginado (RA-04) de las entradas que escribio el profesional autenticado. Cada paciente leido queda en el log de acceso. */
public interface ListarAtencionesUseCase {

    PaginaEntradas ejecutar(Actor actor, int pagina, int tamano);
}
