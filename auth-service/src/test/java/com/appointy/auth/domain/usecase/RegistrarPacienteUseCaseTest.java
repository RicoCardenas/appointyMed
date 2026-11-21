package com.appointy.auth.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.appointy.auth.domain.usecase.CuentaUseCase.RegistrarPacienteComando;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarPacienteUseCaseTest {

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock PacientePerfilRepositoryGateway perfiles;
    @Mock CodificadorClaveGateway codificador;

    final RelojGateway reloj = Dobles.reloj("2026-06-01T10:00:00Z");
    CuentaUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new CuentaUseCase(usuarios, perfiles, codificador, reloj, Dobles.IDS, Dobles.TX);
    }

    private RegistrarPacienteComando comando(String clave, String nacimiento) {
        return new RegistrarPacienteComando("  Ana@Correo.COM ", clave, " 123456 ", " Ana ", " Perez ", nacimiento, "3001234567", "Calle 1", "Cali");
    }

    @Test
    void registraPacienteConCorreoNormalizadoYClaveCodificada() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(false);
        when(codificador.codificar("Clave1234")).thenReturn("hash");
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        PacienteConCuenta r = useCase.registrarPaciente(comando("Clave1234", "1990-05-01"));

        assertEquals("ana@correo.com", r.usuario().correo());
        assertEquals(Rol.PACIENTE, r.usuario().rol());
        assertEquals("hash", r.usuario().claveHash());
        assertTrue(r.usuario().activo());
        assertEquals(reloj.ahora(), r.usuario().creadoEn());
        assertNotNull(UUID.fromString(r.usuario().id()));
        assertEquals("123456", r.perfil().cedula());
        assertEquals("Ana", r.perfil().nombres());
        assertEquals("1990-05-01", r.perfil().fechaNacimiento());
        assertEquals(r.usuario().id(), r.perfil().usuarioId());
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> useCase.registrarPaciente(comando("Clave1234", "1990-05-01")));

        assertEquals("CORREO_YA_REGISTRADO", ex.codigo());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaCedulaYaRegistrada() {
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(false);
        when(perfiles.buscarPorCedula("123456"))
                .thenReturn(new PacientePerfil("otro", "123456", "X", "Y", "1980-01-01", null, null, null));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> useCase.registrarPaciente(comando("Clave1234", "1990-05-01")));

        assertEquals("CEDULA_YA_REGISTRADA", ex.codigo());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaClaveDebilSinTocarLosRepositorios() {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> useCase.registrarPaciente(comando("corta", "1990-05-01")));

        assertEquals("CLAVE_DEBIL", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }

    @Test
    void rechazaFechaDeNacimientoFutura() {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> useCase.registrarPaciente(comando("Clave1234", "2027-01-01")));

        assertEquals("FECHA_NACIMIENTO_INVALIDA", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }

    @Test
    void rechazaFechaConFormatoInvalido() {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> useCase.registrarPaciente(comando("Clave1234", "01/05/1990")));

        assertEquals("FECHA_INVALIDA", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }
}
