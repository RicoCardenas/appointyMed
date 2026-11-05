package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.port.in.LimpiarTokensVencidosPort;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;

public class LimpiarTokensVencidosUseCase implements LimpiarTokensVencidosPort {

    private final TokenRevocadoRepositoryPort revocados;
    private final Clock reloj;

    public LimpiarTokensVencidosUseCase(TokenRevocadoRepositoryPort revocados, Clock reloj) {
        this.revocados = revocados;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public int ejecutar() {
        return revocados.eliminarVencidosAntesDe(reloj.instant());
    }
}
