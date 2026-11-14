package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PaginaUsuarios;

/** Listado paginado de cuentas para el administrativo (RA-04). */
public interface ListarUsuariosUseCase {

    PaginaUsuarios ejecutar(FiltroUsuarios filtro, int pagina, int tamano);
}
