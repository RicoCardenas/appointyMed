package com.appointy.auth.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.PerfilUseCase.ActualizarDatosEditablesComando;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActualizarDatosEditablesUseCaseTest {

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock PacientePerfilRepositoryGateway perfiles;

    final RelojGateway reloj = Dobles.reloj("2026-06-01T10:00:00Z");
    final String id = "id-1";
    final Usuario usuario = new Usuario(id, "ana@correo.com", "hash", Rol.PACIENTE, true, reloj.ahora(), reloj.ahora(), 0L);
    final PacientePerfil perfil = new PacientePerfil(id, "123456", "Ana", "Perez", "1990-05-01", "3000000000", "Calle 1", "Cali");
    PerfilUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new PerfilUseCase(usuarios, perfiles, reloj, Dobles.TX, Dobles.REGISTRO);
    }

    private void existeElPaciente() {
        when(usuarios.buscarPorId(id)).thenReturn(usuario);
        when(perfiles.buscarPorUsuarioId(id)).thenReturn(perfil);
    }

    @Test
    void sinNingunDatoEsUnaValidacionYNoTocaNada() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.actualizarDatosEditables(new ActualizarDatosEditablesComando(id, null, null, null, null)));

        assertEquals("SIN_CAMBIOS", ex.codigo());
        verifyNoInteractions(usuarios, perfiles);
    }

    @Test
    void actualizaContactoYCorreoSinTocarLaIdentidad() {
        existeElPaciente();
        when(usuarios.existePorCorreo("nueva@correo.com")).thenReturn(false);
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        PacienteConCuenta r = useCase.actualizarDatosEditables(new ActualizarDatosEditablesComando(id, " Nueva@Correo.com", "3111111111", null, "Bogota"));

        assertEquals("nueva@correo.com", r.usuario().correo());
        assertEquals("3111111111", r.perfil().telefono());
        assertEquals("Calle 1", r.perfil().direccion());
        assertEquals("Bogota", r.perfil().ciudad());
        assertEquals("123456", r.perfil().cedula());
        assertEquals("Perez", r.perfil().apellidos());
        assertEquals("1990-05-01", r.perfil().fechaNacimiento());
    }

    @Test
    void rechazaUnCorreoQueYaUsaOtraCuenta() {
        existeElPaciente();
        when(usuarios.existePorCorreo("otra@correo.com")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> useCase.actualizarDatosEditables(new ActualizarDatosEditablesComando(id, "otra@correo.com", "3111111111", null, null)));

        assertEquals("CORREO_YA_REGISTRADO", ex.codigo());
        verify(usuarios, never()).guardar(any());
        verify(perfiles, never()).guardar(any());
    }

    @Test
    void elMismoCorreoConOtraCapitalizacionNoEsUnCambio() {
        existeElPaciente();
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        useCase.actualizarDatosEditables(new ActualizarDatosEditablesComando(id, "ANA@correo.com", null, "Carrera 5", null));

        verify(usuarios, never()).existePorCorreo(any());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void fallaSiElUsuarioNoExiste() {
        when(usuarios.buscarPorId(id)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> useCase.actualizarDatosEditables(new ActualizarDatosEditablesComando(id, null, "3111111111", null, null)));
    }
}
