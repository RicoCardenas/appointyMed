package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.PacientePerfil;
import java.util.Optional;
import java.util.UUID;

public interface PacientePerfilRepositoryPort {

    PacientePerfil guardar(PacientePerfil perfil);

    Optional<PacientePerfil> buscarPorUsuarioId(UUID usuarioId);

    Optional<PacientePerfil> buscarPorCedula(String cedula);
}
