package com.appointy.auth.domain.gateway;

import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;

public interface UsuarioRepositoryGateway {

    Usuario guardar(Usuario usuario);

    /** Devuelve null si no existe. */
    Usuario buscarPorId(String id);

    /** Devuelve null si no existe. */
    Usuario buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    boolean existeAlgunoConRol(Rol rol);

    /** Listado paginado (RA-04), del mas reciente al mas antiguo. {@code pagina} empieza en 0. */
    PaginaUsuarios listar(FiltroUsuarios filtro, int pagina, int tamano);

    EstadisticasUsuarios estadisticas();
}
