package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.rule.ReglasUsuarios;
import com.appointy.auth.domain.usecase.CambiarEstadoCuentaUseCase;
import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;

public class CambiarEstadoCuentaUseCaseImpl implements CambiarEstadoCuentaUseCase {

    private final UsuarioRepositoryGateway usuarios;
    private final Clock reloj;

    public CambiarEstadoCuentaUseCaseImpl(UsuarioRepositoryGateway usuarios, Clock reloj) {
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Usuario ejecutar(Comando comando) {
        if (!comando.activo()) {
            ReglasUsuarios.validarDesactivacion(comando.actorId(), comando.usuarioId());
        }
        Usuario usuario = usuarios.buscarPorId(comando.usuarioId());
        if (usuario == null) {
            throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
        }
        if (usuario.activo() == comando.activo()) {
            return usuario;
        }
        return usuarios.guardar(usuario.conActivo(comando.activo(), reloj.millis()));
    }
}
