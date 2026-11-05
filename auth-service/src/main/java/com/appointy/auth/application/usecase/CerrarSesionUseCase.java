package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.port.in.CerrarSesionPort;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class CerrarSesionUseCase implements CerrarSesionPort {

    private final TokenRevocadoRepositoryPort revocados;

    public CerrarSesionUseCase(TokenRevocadoRepositoryPort revocados) {
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
