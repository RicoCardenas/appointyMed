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
import com.appointy.auth.domain.port.in.IniciarSesionPort.Comando;
import com.appointy.auth.domain.port.in.IniciarSesionPort.Resultado;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IniciarSesionUseCaseTest {

    static final Instant AHORA = Instant.parse("2026-06-01T10:00:00Z");

    @Mock UsuarioRepositoryPort usuarios;
    @Mock CodificadorClavePort codificador;
    @Mock EmisorTokenPort emisor;

    IniciarSesionUseCase useCase;

    @BeforeEach
    void iniciar() {
        when(codificador.codificar("senuelo-sin-uso")).thenReturn("hash-senuelo");
        useCase = new IniciarSesionUseCase(usuarios, codificador, emisor);
    }

    private Usuario usuario(boolean activo) {
        return new Usuario(UUID.randomUUID(), "ana@correo.com", "hash", Rol.PACIENTE, activo, AHORA, AHORA, 0L);
    }

    @Test
    void emiteTokenConCredencialesCorrectas() {
        Usuario u = usuario(true);
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(u));
        when(codificador.coincide("Clave1234", "hash")).thenReturn(true);
        when(emisor.emitir(u)).thenReturn(new TokenEmitido("jwt", "jti-1", AHORA.plusSeconds(900)));

        Resultado r = useCase.ejecutar(new Comando("  ANA@correo.com ", "Clave1234"));

        assertEquals("jwt", r.token());
        assertEquals(u.id(), r.usuarioId());
        assertEquals(Rol.PACIENTE, r.rol());
        assertEquals(AHORA.plusSeconds(900), r.expiraEn());
    }

    @Test
    void rechazaClaveIncorrecta() {
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario(true)));
        when(codificador.coincide("mala", "hash")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("ana@correo.com", "mala")));
        verify(emisor, never()).emitir(any());
    }

    @Test
    void conCorreoInexistenteComparaContraElSenueloYRechaza() {
        when(usuarios.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());
        when(codificador.coincide("Clave1234", "hash-senuelo")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("nadie@correo.com", "Clave1234")));
        verify(codificador).coincide("Clave1234", "hash-senuelo");
        verify(emisor, never()).emitir(any());
    }

    @Test
    void rechazaCuentaInactivaAunqueLaClaveSeaCorrecta() {
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario(false)));
        when(codificador.coincide("Clave1234", "hash")).thenReturn(true);

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(new Comando("ana@correo.com", "Clave1234")));
        verify(emisor, never()).emitir(any());
    }
}
