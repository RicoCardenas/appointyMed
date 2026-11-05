package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.usecase.CrearCuentaStaffUseCase;
import com.appointy.auth.domain.usecase.SembrarAdministradorUseCase.Comando;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SembrarYLimpiarUseCaseTest {

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock CrearCuentaStaffUseCase crearCuenta;
    @Mock TokenRevocadoRepositoryGateway revocados;

    @Test
    void creaElAdministradorCuandoNoExisteNinguno() {
        when(usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)).thenReturn(false);

        boolean creado = new SembrarAdministradorUseCaseImpl(usuarios, crearCuenta).ejecutar(new Comando("admin@correo.com", "Clave1234"));

        assertTrue(creado);
        verify(crearCuenta).ejecutar(new CrearCuentaStaffUseCase.Comando("admin@correo.com", "Clave1234", Rol.ADMINISTRATIVO));
    }

    @Test
    void noHaceNadaSiYaHayUnAdministrador() {
        when(usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)).thenReturn(true);

        boolean creado = new SembrarAdministradorUseCaseImpl(usuarios, crearCuenta).ejecutar(new Comando("admin@correo.com", "Clave1234"));

        assertFalse(creado);
        verify(crearCuenta, never()).ejecutar(any());
    }

    @Test
    void limpiezaEliminaLosVencidosAntesDelInstanteActual() {
        Instant ahora = Instant.parse("2026-06-01T10:00:00Z");
        when(revocados.eliminarVencidosAntesDe(ahora.toEpochMilli())).thenReturn(3);

        int eliminados = new LimpiarTokensVencidosUseCaseImpl(revocados, Clock.fixed(ahora, ZoneOffset.UTC)).ejecutar();

        assertEquals(3, eliminados);
    }
}
