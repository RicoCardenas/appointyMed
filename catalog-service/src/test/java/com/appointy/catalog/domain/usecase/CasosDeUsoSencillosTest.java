package com.appointy.catalog.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.rule.ReglasConsulta;
import com.appointy.catalog.domain.usecase.CentroUseCase;
import com.appointy.catalog.domain.usecase.EspecialidadUseCase;
import org.junit.jupiter.api.Test;

/** Los casos de uso de una sola decision: existencia, borrado logico e idempotencia. */
class CasosDeUsoSencillosTest {

    final CentroRepositoryGateway centros = mock(CentroRepositoryGateway.class);
    final EspecialidadRepositoryGateway especialidades = mock(EspecialidadRepositoryGateway.class);
    final ProfesionalRepositoryGateway profesionales = mock(ProfesionalRepositoryGateway.class);
    final HorarioRepositoryGateway horarios = mock(HorarioRepositoryGateway.class);

    static Centro centro(boolean activo) {
        return new Centro("c1", "Sede", "Calle 1", "Bogota", "3001234567", activo);
    }

    static Profesional profesional(boolean activo) {
        return new Profesional("p1", "u1", "Ana", "Perez", "RM-1", activo);
    }

    @Test
    void centros() {
        when(centros.guardar(any())).thenAnswer(i -> i.getArgument(0));
        Centro creado = new CentroUseCase(centros, null, Dobles.TX, Dobles.IDS).crearCentro(new CentroUseCase.CrearCentroComando(" Sede ", " Calle 1 ", " Bogota ", null));
        assertTrue(creado.activo());
        assertEquals("Sede", creado.nombre());

        when(centros.buscarPorId("c1")).thenReturn(centro(true));
        CentroUseCase actualizar = new CentroUseCase(centros, null, Dobles.TX, Dobles.IDS);
        assertEquals("Nueva", actualizar.actualizarCentro(new CentroUseCase.ActualizarCentroComando("c1", " Nueva ", "Calle 2", "Cali", null)).nombre());
        assertThrows(NoEncontradoException.class, () -> actualizar.actualizarCentro(new CentroUseCase.ActualizarCentroComando("x", "n", "d", "c", null)));

        when(centros.listar(true)).thenReturn(new Centro[] {centro(true), centro(false)});
        assertEquals(2, new CentroUseCase(centros, null, Dobles.TX, Dobles.IDS).listarCentros(true).length);

        CentroUseCase desactivar = new CentroUseCase(centros, null, Dobles.TX, Dobles.IDS);
        desactivar.desactivarCentro("c1");
        verify(centros, org.mockito.Mockito.times(3)).guardar(any());
        when(centros.buscarPorId("c2")).thenReturn(centro(false));
        desactivar.desactivarCentro("c2");
        verify(centros, org.mockito.Mockito.times(3)).guardar(any());
        assertThrows(NoEncontradoException.class, () -> desactivar.desactivarCentro("x"));
    }

    @Test
    void especialidades() {
        when(especialidades.listar(true)).thenReturn(new Especialidad[] {new Especialidad("e1", "Cardiologia", null, false)});
        assertEquals(1, new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS).listarEspecialidades(true).length);

        when(especialidades.buscarPorId("e1")).thenReturn(new Especialidad("e1", "Cardiologia", null, true));
        when(especialidades.buscarPorId("e2")).thenReturn(new Especialidad("e2", "Pediatria", null, false));
        when(especialidades.buscarPorNombre("Pediatria")).thenReturn(new Especialidad("e2", "Pediatria", null, true));
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));
        EspecialidadUseCase actualizar = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS);
        assertEquals("Cardio", actualizar.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("e1", " Cardio ", "d")).nombre());
        assertThrows(ConflictoException.class, () -> actualizar.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("e1", "Pediatria", "d")));
        assertThrows(ValidacionException.class, () -> actualizar.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("e1", " ", "d")));
        assertThrows(NoEncontradoException.class, () -> actualizar.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("x", "n", "d")));

        EspecialidadUseCase desactivar = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS);
        desactivar.desactivarEspecialidad("e1");
        desactivar.desactivarEspecialidad("e2");
        verify(especialidades, org.mockito.Mockito.times(2)).guardar(any());
        assertThrows(NoEncontradoException.class, () -> desactivar.desactivarEspecialidad("x"));
    }

    @Test
    void profesionales() {
        when(profesionales.buscarPorId("p1")).thenReturn(profesional(true));
        when(profesionales.buscarPorId("p2")).thenReturn(profesional(false));
        when(profesionales.idsEspecialidades("p1")).thenReturn(new String[] {"e1"});
        when(profesionales.idsCentros("p1")).thenReturn(new String[] {"c1", "c2"});
        assertEquals(2, new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS).consultarProfesional("p1").centroIds().length);
        assertThrows(NoEncontradoException.class, () -> new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS).consultarProfesional("x"));

        ProfesionalUseCase desactivar = new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS);
        desactivar.desactivarProfesional("p1");
        desactivar.desactivarProfesional("p2");
        verify(profesionales, org.mockito.Mockito.times(1)).guardar(any());
        assertThrows(NoEncontradoException.class, () -> desactivar.desactivarProfesional("x"));

        when(profesionales.buscarPorUsuarioId("u1")).thenReturn(profesional(true));
        assertEquals("p1", new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS).buscarProfesionalPorUsuario("u1").id());
        assertNull(new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS).buscarProfesionalPorUsuario("nadie"));
    }

    @Test
    void listadosDeUnProfesional() {
        when(profesionales.buscarPorId("p1")).thenReturn(profesional(true));
        when(centros.listarActivosDeProfesional("p1")).thenReturn(new Centro[] {centro(true)});
        when(horarios.listarDeProfesional("p1")).thenReturn(new HorarioAtencion[] {new HorarioAtencion("h1", "p1", "c1", 1, 480, 540, 30, true)});
        assertEquals(1, new CentroUseCase(centros, profesionales, Dobles.TX, Dobles.IDS).listarCentrosDeProfesional("p1").length);
        assertEquals(1, new HorarioUseCase(profesionales, null, horarios, null, null, null, null, null, Dobles.TX, Dobles.IDS).listarHorarios("p1").length);
        assertThrows(NoEncontradoException.class, () -> new CentroUseCase(centros, profesionales, Dobles.TX, Dobles.IDS).listarCentrosDeProfesional("x"));
        assertThrows(NoEncontradoException.class, () -> new HorarioUseCase(profesionales, null, horarios, null, null, null, null, null, Dobles.TX, Dobles.IDS).listarHorarios("x"));
    }

    @Test
    void parametros() {
        ParametroRepositoryGateway parametros = mock(ParametroRepositoryGateway.class);
        when(parametros.listar()).thenReturn(new ParametroServicio[] {new ParametroServicio("k", "v", null)});
        assertEquals("v", new ParametroUseCase(parametros, null, Dobles.TX).consultarParametros()[0].valor());
    }

    @Test
    void listadoPaginadoDeProfesionalesYFestivos() {
        when(profesionales.listar(new FiltroProfesionales("ana", true), 0, 20))
                .thenReturn(new PaginaProfesionales(new Profesional[] {profesional(true)}, 0, 20, 1, 1));
        ProfesionalUseCase listar = new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS);
        assertEquals(1, listar.listarProfesionales(new FiltroProfesionales("ana", true), 0, 20).totalElementos());
        assertThrows(ValidacionException.class, () -> listar.listarProfesionales(null, -1, 20));
        assertThrows(ValidacionException.class, () -> listar.listarProfesionales(null, 0, 0));
        assertThrows(ValidacionException.class, () -> listar.listarProfesionales(null, 0, 101));
        verify(profesionales, never()).listar(null, -1, 20);

        FestivosClientGateway festivos = mock(FestivosClientGateway.class);
        when(festivos.festivos(2026)).thenReturn(new Festivo[] {new Festivo("2026-01-01", "Anio Nuevo")});
        ParametroUseCase consultar = new ParametroUseCase(null, festivos, Dobles.TX);
        assertEquals("2026-01-01", consultar.consultarFestivos(2026)[0].fecha());
        assertEquals("ANIO_INVALIDO", assertThrows(ValidacionException.class, () -> consultar.consultarFestivos(1999)).codigo());
        assertThrows(ValidacionException.class, () -> consultar.consultarFestivos(2101));
        ReglasConsulta.validarAnio(2000);
        ReglasConsulta.validarPaginacion(0, 100);
    }
}
