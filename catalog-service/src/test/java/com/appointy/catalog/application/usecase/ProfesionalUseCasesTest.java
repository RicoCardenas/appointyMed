package com.appointy.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.ActualizarProfesionalPort;
import com.appointy.catalog.domain.port.in.CrearProfesionalPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfesionalUseCasesTest {

    @Mock ProfesionalRepositoryPort profesionales;
    @Mock EspecialidadRepositoryPort especialidades;

    final Profesional ana = new Profesional("p1", "u1", "Ana", "Perez", "RM-1", true);

    @Test
    void creaUnProfesionalActivoYLoVinculaALaCuentaPorValor() {
        when(profesionales.buscarPorUsuarioId("u9")).thenReturn(null);
        when(profesionales.buscarPorRegistroMedico("RM-9")).thenReturn(null);
        when(profesionales.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Profesional p = new CrearProfesionalUseCase(profesionales).ejecutar(new CrearProfesionalPort.Comando("u9", " Luis ", " Gomez ", " RM-9 "));

        assertEquals("u9", p.usuarioId());
        assertEquals("Luis", p.nombres());
        assertEquals("RM-9", p.registroMedico());
        assertTrue(p.activo());
    }

    @Test
    void noCreaSiLaCuentaOElRegistroYaTienenPerfil() {
        when(profesionales.buscarPorUsuarioId("u1")).thenReturn(ana);
        when(profesionales.buscarPorUsuarioId("u2")).thenReturn(null);
        when(profesionales.buscarPorRegistroMedico("RM-1")).thenReturn(ana);
        CrearProfesionalUseCase useCase = new CrearProfesionalUseCase(profesionales);

        assertEquals("PROFESIONAL_YA_EXISTE",
                assertThrows(ConflictoException.class, () -> useCase.ejecutar(new CrearProfesionalPort.Comando("u1", "A", "B", "RM-5"))).codigo());
        assertEquals("REGISTRO_MEDICO_YA_REGISTRADO",
                assertThrows(ConflictoException.class, () -> useCase.ejecutar(new CrearProfesionalPort.Comando("u2", "A", "B", "RM-1"))).codigo());
        verify(profesionales, never()).guardar(any());
    }

    @Test
    void actualizarNoCambiaLaCuentaVinculada() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(profesionales.buscarPorRegistroMedico("RM-1")).thenReturn(ana);
        when(profesionales.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Profesional p = new ActualizarProfesionalUseCase(profesionales).ejecutar(new ActualizarProfesionalPort.Comando("p1", "Ana Maria", "Perez", "RM-1"));

        assertEquals("u1", p.usuarioId());
        assertEquals("Ana Maria", p.nombres());
    }

    @Test
    void actualizarRechazaElRegistroDeOtroYUnProfesionalInexistente() {
        when(profesionales.buscarPorId("p2")).thenReturn(new Profesional("p2", "u2", "B", "C", "RM-2", true));
        when(profesionales.buscarPorRegistroMedico("RM-1")).thenReturn(ana);
        when(profesionales.buscarPorId("nada")).thenReturn(null);
        ActualizarProfesionalUseCase useCase = new ActualizarProfesionalUseCase(profesionales);

        assertThrows(ConflictoException.class, () -> useCase.ejecutar(new ActualizarProfesionalPort.Comando("p2", "B", "C", "RM-1")));
        assertThrows(NoEncontradoException.class, () -> useCase.ejecutar(new ActualizarProfesionalPort.Comando("nada", "B", "C", "RM-3")));
    }

    @Test
    void desactivarEsLogico() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(profesionales.guardar(any())).thenAnswer(i -> i.getArgument(0));

        new DesactivarProfesionalUseCase(profesionales).ejecutar("p1");

        verify(profesionales).guardar(new Profesional("p1", "u1", "Ana", "Perez", "RM-1", false));
    }

    @Test
    void listarPorEspecialidadExigeUnaEspecialidadActiva() {
        when(especialidades.buscarPorId("e1")).thenReturn(new Especialidad("e1", "A", null, true));
        when(profesionales.listarActivosPorEspecialidad("e1")).thenReturn(new Profesional[] {ana});
        when(especialidades.buscarPorId("e2")).thenReturn(new Especialidad("e2", "B", null, false));
        ListarProfesionalesPorEspecialidadUseCase useCase = new ListarProfesionalesPorEspecialidadUseCase(especialidades, profesionales);

        assertEquals(1, useCase.ejecutar("e1").length);
        assertThrows(NoEncontradoException.class, () -> useCase.ejecutar("e2"));
    }
}
