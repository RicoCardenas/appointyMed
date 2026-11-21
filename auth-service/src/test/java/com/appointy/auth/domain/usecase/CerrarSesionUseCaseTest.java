package com.appointy.auth.domain.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.usecase.SesionUseCase.CerrarSesionComando;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CerrarSesionUseCaseTest {

    @Mock TokenRevocadoRepositoryGateway revocados;
    @Mock CodificadorClaveGateway codificador;
    SesionUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new SesionUseCase(null, codificador, null, revocados, null, Dobles.TX);
    }

    final long expira = 1_780_000_000_000L;

    @Test
    void revocaElTokenHastaSuVencimiento() {
        when(revocados.existePorJti("jti-1")).thenReturn(false);

        useCase.cerrarSesion(new CerrarSesionComando("jti-1", expira));

        verify(revocados).guardar(new TokenRevocado("jti-1", expira));
    }

    @Test
    void esIdempotenteSiYaEstabaRevocado() {
        when(revocados.existePorJti("jti-1")).thenReturn(true);

        useCase.cerrarSesion(new CerrarSesionComando("jti-1", expira));

        verify(revocados, never()).guardar(any());
    }
}
