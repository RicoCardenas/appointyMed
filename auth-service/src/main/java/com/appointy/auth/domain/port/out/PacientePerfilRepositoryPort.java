package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.PacientePerfil;

public interface PacientePerfilRepositoryPort {

    PacientePerfil guardar(PacientePerfil perfil);

    /** Devuelve null si no existe. */
    PacientePerfil buscarPorUsuarioId(String usuarioId);

    /** Devuelve null si no existe. */
    PacientePerfil buscarPorCedula(String cedula);
}
