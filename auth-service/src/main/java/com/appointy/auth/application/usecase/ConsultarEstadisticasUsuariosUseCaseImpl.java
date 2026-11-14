package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.usecase.ConsultarEstadisticasUsuariosUseCase;

public class ConsultarEstadisticasUsuariosUseCaseImpl implements ConsultarEstadisticasUsuariosUseCase {

    private final UsuarioRepositoryGateway usuarios;

    public ConsultarEstadisticasUsuariosUseCaseImpl(UsuarioRepositoryGateway usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public EstadisticasUsuarios ejecutar() {
        return usuarios.estadisticas();
    }
}
