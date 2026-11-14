package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Pagina;

/** Listado paginado (RA-04), del mas reciente al mas antiguo. */
public interface ListarNotificacionesUseCase {

    Pagina ejecutar(FiltroNotificaciones filtro, int pagina, int tamano);
}
