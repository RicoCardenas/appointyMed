package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.usecase.CerrarSesionUseCase;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class CerrarSesionUseCaseImpl implements CerrarSesionUseCase {

    private final TokenRevocadoRepositoryGateway revocados;

    public CerrarSesionUseCaseImpl(TokenRevocadoRepositoryGateway revocados) {
        this.revocados = revocados;
    }

    @Override
    @Transactional
    public void ejecutar(Comando comando) {
        if (!revocados.existePorJti(comando.jti())) {
            revocados.guardar(new TokenRevocado(comando.jti(), comando.expiraEn()));
        }
    }
}
