package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.usecase.LimpiarTokensVencidosUseCase;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;

public class LimpiarTokensVencidosUseCaseImpl implements LimpiarTokensVencidosUseCase {

    private final TokenRevocadoRepositoryGateway revocados;
    private final Clock reloj;

    public LimpiarTokensVencidosUseCaseImpl(TokenRevocadoRepositoryGateway revocados, Clock reloj) {
        this.revocados = revocados;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public int ejecutar() {
        return revocados.eliminarVencidosAntesDe(reloj.millis());
    }
}
