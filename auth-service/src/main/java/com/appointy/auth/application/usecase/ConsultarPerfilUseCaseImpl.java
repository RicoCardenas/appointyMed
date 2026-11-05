package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.ConsultarPerfilUseCase;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarPerfilUseCaseImpl implements ConsultarPerfilUseCase {

    private final UsuarioRepositoryGateway usuarios;
    private final PacientePerfilRepositoryGateway perfiles;

    public ConsultarPerfilUseCaseImpl(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteConCuenta ejecutar(String usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
        }
        PacientePerfil perfil = perfiles.buscarPorUsuarioId(usuarioId);
        if (perfil == null) {
            throw new NoEncontradoException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil de paciente");
        }
        return new PacienteConCuenta(usuario, perfil);
    }
}
