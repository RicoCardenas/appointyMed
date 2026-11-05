package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.ConsultarPerfilPort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarPerfilUseCase implements ConsultarPerfilPort {

    private final UsuarioRepositoryPort usuarios;
    private final PacientePerfilRepositoryPort perfiles;

    public ConsultarPerfilUseCase(UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteConCuenta ejecutar(UUID usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
        PacientePerfil perfil = perfiles.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil de paciente"));
        return new PacienteConCuenta(usuario, perfil);
    }
}
