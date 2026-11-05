package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.CorregirDatosInmutablesPort.Comando;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorregirDatosInmutablesUseCaseTest {

    @Mock UsuarioRepositoryPort usuarios;
    @Mock PacientePerfilRepositoryPort perfiles;

    final Clock reloj = Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC);
    final String actor = "admin-1";
    final String id = "id-1";
    final Usuario usuario = new Usuario(id, "ana@correo.com", "hash", Rol.PACIENTE, true, reloj.millis(), reloj.millis(), 0L);
    final PacientePerfil perfil = new PacientePerfil(id, "123456", "Ana", "Perez", "1990-05-01", "3000000000", "Calle 1", "Cali");
    CorregirDatosInmutablesUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new CorregirDatosInmutablesUseCase(usuarios, perfiles, reloj);
    }

    private void existeElPaciente() {
        when(usuarios.buscarPorId(id)).thenReturn(usuario);
        when(perfiles.buscarPorUsuarioId(id)).thenReturn(perfil);
    }

    @Test
    void exigeAlMenosUnDato() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.ejecutar(new Comando(actor, id, null, null, null, null)));
        assertEquals("SIN_CAMBIOS", ex.codigo());
    }

    @Test
    void corrigeNombresSinTocarLoDemas() {
        existeElPaciente();
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        PacienteConCuenta r = useCase.ejecutar(new Comando(actor, id, null, " Ana Maria ", null, null));

        assertEquals("Ana Maria", r.perfil().nombres());
        assertEquals("123456", r.perfil().cedula());
        assertEquals("Perez", r.perfil().apellidos());
        assertEquals("3000000000", r.perfil().telefono());
    }

    @Test
    void corrigeLaFechaDeNacimientoYLaNormaliza() {
        existeElPaciente();
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        PacienteConCuenta r = useCase.ejecutar(new Comando(actor, id, null, null, null, "1991-06-02"));

        assertEquals("1991-06-02", r.perfil().fechaNacimiento());
    }

    @Test
    void rechazaUnaCedulaQuePerteneceAOtroPaciente() {
        existeElPaciente();
        when(perfiles.buscarPorCedula("999999"))
                .thenReturn(new PacientePerfil("otro", "999999", "X", "Y", "1970-01-01", null, null, null));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> useCase.ejecutar(new Comando(actor, id, "999999", null, null, null)));

        assertEquals("CEDULA_YA_REGISTRADA", ex.codigo());
        verify(perfiles, never()).guardar(any());
    }

    @Test
    void laMismaCedulaNoSeConsultaComoDuplicada() {
        existeElPaciente();
        when(perfiles.guardar(any())).thenAnswer(i -> i.getArgument(0));

        useCase.ejecutar(new Comando(actor, id, "123456", null, null, null));

        verify(perfiles, never()).buscarPorCedula(any());
    }

    @Test
    void rechazaFechaFuturaOMalFormada() {
        assertThrows(ValidacionException.class, () -> useCase.ejecutar(new Comando(actor, id, null, null, null, "2027-01-01")));
        assertThrows(ValidacionException.class, () -> useCase.ejecutar(new Comando(actor, id, null, null, null, "ayer")));
    }

    @Test
    void fallaSiElPacienteNoExiste() {
        when(usuarios.buscarPorId(id)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> useCase.ejecutar(new Comando(actor, id, "123456", null, null, null)));
    }
}
