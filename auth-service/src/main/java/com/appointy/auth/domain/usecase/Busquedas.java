package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;

/** Auxiliar: búsquedas que deben existir; si no, lanzan la excepción de negocio. */
final class Busquedas {

    private Busquedas() {}

    static Usuario usuario(UsuarioRepositoryGateway usuarios, String id) {
        Usuario usuario = usuarios.buscarPorId(id);
        if (usuario == null) {
            throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
        }
        return usuario;
    }

    static PacientePerfil perfil(PacientePerfilRepositoryGateway perfiles, String usuarioId) {
        PacientePerfil perfil = perfiles.buscarPorUsuarioId(usuarioId);
        if (perfil == null) {
            throw new NoEncontradoException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil de paciente");
        }
        return perfil;
    }
}
