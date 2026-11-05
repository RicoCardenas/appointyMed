package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    /** Devuelve null si no existe. */
    Usuario buscarPorId(String id);

    /** Devuelve null si no existe. */
    Usuario buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    boolean existeAlgunoConRol(Rol rol);
}
