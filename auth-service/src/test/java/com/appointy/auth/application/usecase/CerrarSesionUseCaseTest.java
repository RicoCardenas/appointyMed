package com.appointy.auth.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.port.in.CerrarSesionPort.Comando;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CerrarSesionUseCaseTest {

    @Mock TokenRevocadoRepositoryPort revocados;
    @InjectMocks CerrarSesionUseCase useCase;

    final long expira = 1_780_000_000_000L;

    @Test
    void revocaElTokenHastaSuVencimiento() {
        when(revocados.existePorJti("jti-1")).thenReturn(false);

        useCase.ejecutar(new Comando("jti-1", expira));

        verify(revocados).guardar(new TokenRevocado("jti-1", expira));
    }

    @Test
    void esIdempotenteSiYaEstabaRevocado() {
        when(revocados.existePorJti("jti-1")).thenReturn(true);

        useCase.ejecutar(new Comando("jti-1", expira));

        verify(revocados, never()).guardar(any());
    }
}
