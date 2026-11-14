package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.CambiarClaveUseCase;
import com.appointy.auth.domain.usecase.CambiarEstadoCuentaUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdministracionUsuariosTest {

    final UsuarioRepositoryGateway usuarios = mock(UsuarioRepositoryGateway.class);
    final CodificadorClaveGateway codificador = mock(CodificadorClaveGateway.class);
    final Clock reloj = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);

    static Usuario usuario(String id, boolean activo) {
        return new Usuario(id, id + "@correo.co", "hash-viejo", Rol.PACIENTE, activo, 1, 1, 0L);
    }

    @Test
    void listaConPaginacionValida() {
        when(usuarios.listar(new FiltroUsuarios(Rol.PACIENTE, true), 1, 5)).thenReturn(new PaginaUsuarios(new Usuario[] {usuario("u1", true)}, 1, 5, 6, 2));
        ListarUsuariosUseCaseImpl caso = new ListarUsuariosUseCaseImpl(usuarios);
        assertEquals(6, caso.ejecutar(new FiltroUsuarios(Rol.PACIENTE, true), 1, 5).totalElementos());
        assertThrows(ValidacionException.class, () -> caso.ejecutar(null, -1, 5));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(null, 0, 0));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(null, 0, 101));
    }

    @Test
    void estadisticasDelegaAlPuerto() {
        when(usuarios.estadisticas()).thenReturn(new EstadisticasUsuarios(5, 2, 1, 7, 1));
        assertEquals(7, new ConsultarEstadisticasUsuariosUseCaseImpl(usuarios).ejecutar().activos());
    }

    @Test
    void desactivarYActivarUnaCuenta() {
        CambiarEstadoCuentaUseCaseImpl caso = new CambiarEstadoCuentaUseCaseImpl(usuarios, reloj);
        when(usuarios.buscarPorId("u1")).thenReturn(usuario("u1", true));
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        Usuario desactivado = caso.ejecutar(new CambiarEstadoCuentaUseCase.Comando("admin", "u1", false));
        assertFalse(desactivado.activo());
        assertEquals(reloj.millis(), desactivado.actualizadoEn());
        when(usuarios.buscarPorId("u2")).thenReturn(usuario("u2", false));
        assertTrue(caso.ejecutar(new CambiarEstadoCuentaUseCase.Comando("admin", "u2", true)).activo());
    }

    @Test
    void repetirLaOperacionNoEscribeYLasReglasSeAplican() {
        CambiarEstadoCuentaUseCaseImpl caso = new CambiarEstadoCuentaUseCaseImpl(usuarios, reloj);
        when(usuarios.buscarPorId("u1")).thenReturn(usuario("u1", true));
        caso.ejecutar(new CambiarEstadoCuentaUseCase.Comando("admin", "u1", true));
        verify(usuarios, never()).guardar(any());
        assertEquals("NO_AUTODESACTIVAR", assertThrows(ConflictoException.class, () -> caso.ejecutar(new CambiarEstadoCuentaUseCase.Comando("admin", "admin", false))).codigo());
        assertThrows(NoEncontradoException.class, () -> caso.ejecutar(new CambiarEstadoCuentaUseCase.Comando("admin", "nadie", false)));
    }

    @Test
    void cambiarLaClaveValidaLaActualYGuardaElNuevoHash() {
        CambiarClaveUseCaseImpl caso = new CambiarClaveUseCaseImpl(usuarios, codificador, reloj);
        when(usuarios.buscarPorId("u1")).thenReturn(usuario("u1", true));
        when(codificador.coincide("Actual123", "hash-viejo")).thenReturn(true);
        when(codificador.codificar("Nueva456")).thenReturn("hash-nuevo");
        caso.ejecutar(new CambiarClaveUseCase.Comando("u1", "Actual123", "Nueva456"));
        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).guardar(guardado.capture());
        assertEquals("hash-nuevo", guardado.getValue().claveHash());
    }

    @Test
    void cambiarLaClaveRechazaClavesInvalidasIncorrectasOUsuariosInexistentes() {
        CambiarClaveUseCaseImpl caso = new CambiarClaveUseCaseImpl(usuarios, codificador, reloj);
        when(usuarios.buscarPorId("u1")).thenReturn(usuario("u1", true));
        assertEquals("CLAVE_ACTUAL_INCORRECTA",
                assertThrows(ValidacionException.class, () -> caso.ejecutar(new CambiarClaveUseCase.Comando("u1", "Mala12345", "Nueva456"))).codigo());
        assertEquals("CLAVE_DEBIL", assertThrows(ValidacionException.class, () -> caso.ejecutar(new CambiarClaveUseCase.Comando("u1", "Actual123", "corta"))).codigo());
        assertEquals("CLAVE_IGUAL", assertThrows(ValidacionException.class, () -> caso.ejecutar(new CambiarClaveUseCase.Comando("u1", "Actual123", "Actual123"))).codigo());
        assertThrows(NoEncontradoException.class, () -> caso.ejecutar(new CambiarClaveUseCase.Comando("nadie", "Actual123", "Nueva456")));
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void consultarElPerfilPropio() {
        PacientePerfilRepositoryGateway perfiles = mock(PacientePerfilRepositoryGateway.class);
        ConsultarPerfilUseCaseImpl caso = new ConsultarPerfilUseCaseImpl(usuarios, perfiles);
        when(usuarios.buscarPorId("u1")).thenReturn(usuario("u1", true));
        when(perfiles.buscarPorUsuarioId("u1")).thenReturn(new PacientePerfil("u1", "123", "Ana", "Perez", "1990-01-01", null, null, null));
        PacienteConCuenta p = caso.ejecutar("u1");
        assertEquals("Ana", p.perfil().nombres());
        assertThrows(NoEncontradoException.class, () -> caso.ejecutar("nadie"));
        when(usuarios.buscarPorId("u3")).thenReturn(usuario("u3", true));
        assertEquals("PERFIL_NO_ENCONTRADO", assertThrows(NoEncontradoException.class, () -> caso.ejecutar("u3")).codigo());
    }

    @Test
    void nombresDePacientesSoloDevuelveNombres() {
        PacientePerfilRepositoryGateway perfiles = mock(PacientePerfilRepositoryGateway.class);
        when(perfiles.buscarPorUsuarioIds(new String[] {"u1", "u2"})).thenReturn(new PacientePerfil[] {new PacientePerfil("u1", "123", "Ana", "Perez", "1990-01-01", "300", "Calle", "Bogota")});
        ConsultarNombresPacientesUseCaseImpl caso = new ConsultarNombresPacientesUseCaseImpl(perfiles);
        com.appointy.auth.domain.model.NombrePaciente[] r = caso.ejecutar(new String[] {"u1", "u2"});
        assertEquals(1, r.length);
        assertEquals("Perez", r[0].apellidos());
        assertThrows(ValidacionException.class, () -> caso.ejecutar(new String[0]));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(null));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(new String[51]));
    }
}
