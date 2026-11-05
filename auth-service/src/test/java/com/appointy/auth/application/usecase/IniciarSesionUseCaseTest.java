package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.CredencialesInvalidasException;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.IniciarSesionUseCase.Comando;
import com.appointy.auth.domain.usecase.IniciarSesionUseCase.Resultado;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IniciarSesionUseCaseTest {

    static final long AHORA = Instant.parse("2026-06-01T10:00:00Z").toEpochMilli();

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock CodificadorClaveGateway codificador;
    @Mock EmisorTokenGateway emisor;

    IniciarSesionUseCaseImpl useCase;

    @BeforeEach
    void iniciar() {
        when(codificador.codificar("senuelo-sin-uso")).thenReturn("hash-senuelo");
        useCase = new IniciarSesionUseCaseImpl(usuarios, codificador, emisor);
    }

    private Usuario usuario(boolean activo) {
        return new Usuario("id-1", "ana@correo.com", "hash", Rol.PACIENTE, activo, AHORA, AHORA, 0L);
    }

    @Test
    void emiteTokenConCredencialesCorrectas() {
        Usuario u = usuario(true);
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(u);
        when(codificador.coincide("Clave1234", "hash")).thenReturn(true);
        when(emisor.emitir(u)).thenReturn(new TokenEmitido("jwt", "jti-1", AHORA + 900_000));

        Resultado r = useCase.ejecutar(new Comando("  ANA@correo.com ", "Clave1234"));

        assertEquals("jwt", r.token());
        assertEquals("id-1", r.usuarioId());
        assertEquals(Rol.PACIENTE, r.rol());
        assertEquals(AHORA + 900_000, r.expiraEn());
    }

    @Test
    void rechazaClaveIncorrecta() {
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(usuario(true));
        when(codificador.coincide("mala", "hash")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("ana@correo.com", "mala")));
        verify(emisor, never()).emitir(any());
    }

    @Test
    void conCorreoInexistenteComparaContraElSenueloYRechaza() {
        when(usuarios.buscarPorCorreo("nadie@correo.com")).thenReturn(null);
        when(codificador.coincide("Clave1234", "hash-senuelo")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("nadie@correo.com", "Clave1234")));
        verify(codificador).coincide("Clave1234", "hash-senuelo");
        verify(emisor, never()).emitir(any());
    }

    @Test
    void rechazaCuentaInactivaAunqueLaClaveSeaCorrecta() {
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(usuario(false));
        when(codificador.coincide("Clave1234", "hash")).thenReturn(true);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("ana@correo.com", "Clave1234")));
        verify(emisor, never()).emitir(any());
    }
}
