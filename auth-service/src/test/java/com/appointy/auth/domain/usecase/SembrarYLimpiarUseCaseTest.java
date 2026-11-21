package com.appointy.auth.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.CuentaUseCase.SembrarAdministradorComando;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SembrarYLimpiarUseCaseTest {

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock PacientePerfilRepositoryGateway perfiles;
    @Mock CodificadorClaveGateway codificador;
    @Mock TokenRevocadoRepositoryGateway revocados;

    final RelojGateway reloj = Dobles.reloj("2026-06-01T10:00:00Z");

    @Test
    void creaElAdministradorCuandoNoExisteNinguno() {
        when(usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)).thenReturn(false);
        when(codificador.codificar("Clave1234")).thenReturn("hash");
        when(usuarios.guardar(org.mockito.ArgumentMatchers.any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        boolean creado = new CuentaUseCase(usuarios, perfiles, codificador, reloj, Dobles.IDS, Dobles.TX)
                .sembrarAdministrador(new SembrarAdministradorComando("admin@correo.com", "Clave1234"));

        assertTrue(creado);
        verify(usuarios).guardar(org.mockito.ArgumentMatchers.argThat(u -> u.rol() == Rol.ADMINISTRATIVO && u.correo().equals("admin@correo.com")));
    }

    @Test
    void noHaceNadaSiYaHayUnAdministrador() {
        when(usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)).thenReturn(true);

        boolean creado = new CuentaUseCase(usuarios, perfiles, codificador, reloj, Dobles.IDS, Dobles.TX)
                .sembrarAdministrador(new SembrarAdministradorComando("admin@correo.com", "Clave1234"));

        assertFalse(creado);
        verify(usuarios, never()).guardar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void limpiezaEliminaLosVencidosAntesDelInstanteActual() {
        Instant ahora = Instant.parse("2026-06-01T10:00:00Z");
        when(revocados.eliminarVencidosAntesDe(ahora.toEpochMilli())).thenReturn(3);

        int eliminados = new SesionUseCase(usuarios, codificador, null, revocados, Dobles.reloj(ahora.toString()), Dobles.TX).limpiarTokensVencidos();

        assertEquals(3, eliminados);
    }
}
