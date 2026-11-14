package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.PaginaCitasConFranja;

/** Listado paginado de todas las citas (RA-04), de la mas reciente a la mas antigua. Solo ADMINISTRATIVO. */
public interface ListarCitasUseCase {

    PaginaCitasConFranja ejecutar(Actor actor, FiltroCitas filtro, int pagina, int tamano);
}
