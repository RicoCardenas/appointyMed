package com.appointy.auth.domain.gateway;

import com.appointy.auth.domain.model.PacientePerfil;

public interface PacientePerfilRepositoryGateway {

    PacientePerfil guardar(PacientePerfil perfil);

    /** Devuelve null si no existe. */
    PacientePerfil buscarPorUsuarioId(String usuarioId);

    /** Devuelve null si no existe. */
    PacientePerfil buscarPorCedula(String cedula);

    /** Perfiles de los usuarios pedidos; los ids sin perfil no aparecen. */
    PacientePerfil[] buscarPorUsuarioIds(String[] usuarioIds);
}
