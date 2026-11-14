package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.PaginaProfesionales;

/** Listado paginado de profesionales (RA-04), ordenado por apellidos y nombres. */
public interface ListarProfesionalesUseCase {

    PaginaProfesionales ejecutar(FiltroProfesionales filtro, int pagina, int tamano);
}
