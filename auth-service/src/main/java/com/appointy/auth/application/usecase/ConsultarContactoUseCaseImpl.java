package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.ConsultarContactoUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarContactoUseCaseImpl implements ConsultarContactoUseCase {

    private final UsuarioRepositoryGateway usuarios;

    public ConsultarContactoUseCaseImpl(UsuarioRepositoryGateway usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public String ejecutar(String usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId);
        if (usuario == null || !usuario.activo()) {
            throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
        }
        return usuario.correo();
    }
}
