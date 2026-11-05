package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.port.in.RegistrarPacientePort.Comando;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarPacienteUseCaseTest {

    @Mock UsuarioRepositoryPort usuarios;
    @Mock PacientePerfilRepositoryPort perfiles;
    @Mock CodificadorClavePort codificador;

    final Clock reloj = Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC);
    RegistrarPacienteUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new RegistrarPacienteUseCase(usuarios, perfiles, codificador, reloj);
    }

    private Comando comando(String clave, LocalDate nacimiento) {
        return new Comando("  Ana@Correo.COM ", clave, " 123456 ", " Ana ", " Perez ", nacimiento, "3001234567", "Calle 1", "Cali");
    }

    @Test
    void registraPacienteConCorreoNormalizadoYClaveCodificada() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(false);
        when(perfiles.buscarPorCedula("123456")).thenReturn(Optional.empty());
        when(codificador.codificar("Clave1234")).thenReturn("hash");
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        PacienteConCuenta r = useCase.ejecutar(comando("Clave1234", LocalDate.of(1990, 5, 1)));

        assertEquals("ana@correo.com", r.usuario().correo());
        assertEquals(Rol.PACIENTE, r.usuario().rol());
        assertEquals("hash", r.usuario().claveHash());
        assertTrue(r.usuario().activo());
        assertEquals("123456", r.perfil().cedula());
        assertEquals("Ana", r.perfil().nombres());
        assertEquals(r.usuario().id(), r.perfil().usuarioId());
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> useCase.ejecutar(comando("Clave1234", LocalDate.of(1990, 5, 1))));

        assertEquals("CORREO_YA_REGISTRADO", ex.codigo());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaCedulaYaRegistrada() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(false);
        PacientePerfil existente = new PacientePerfil(UUID.randomUUID(), "123456", "X", "Y", LocalDate.of(1980, 1, 1), null, null, null);
        when(perfiles.buscarPorCedula("123456")).thenReturn(Optional.of(existente));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> useCase.ejecutar(comando("Clave1234", LocalDate.of(1990, 5, 1))));

        assertEquals("CEDULA_YA_REGISTRADA", ex.codigo());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaClaveDebilSinTocarLosRepositorios() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.ejecutar(comando("corta", LocalDate.of(1990, 5, 1))));

        assertEquals("CLAVE_DEBIL", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }

    @Test
    void rechazaFechaDeNacimientoFutura() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.ejecutar(comando("Clave1234", LocalDate.of(2027, 1, 1))));

        assertEquals("FECHA_NACIMIENTO_INVALIDA", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }
}
