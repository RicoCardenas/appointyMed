package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    boolean existeAlgunoConRol(Rol rol);
}
