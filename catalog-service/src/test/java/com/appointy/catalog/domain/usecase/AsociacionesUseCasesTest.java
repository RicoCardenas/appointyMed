package com.appointy.catalog.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsociacionesUseCasesTest {

    @Mock ProfesionalRepositoryGateway profesionales;
    @Mock EspecialidadRepositoryGateway especialidades;
    @Mock CentroRepositoryGateway centros;
    @Mock HorarioRepositoryGateway horarios;
    @Mock BloqueoRepositoryGateway bloqueos;

    final Profesional ana = new Profesional("p1", "u1", "Ana", "Perez", "RM-1", true);

    @Test
    void asociaEspecialidadesSinDuplicadosYDevuelveElDetalle() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(especialidades.buscarPorId("e1")).thenReturn(new Especialidad("e1", "A", null, true));
        when(especialidades.buscarPorId("e2")).thenReturn(new Especialidad("e2", "B", null, true));
        when(profesionales.idsCentros("p1")).thenReturn(new String[] {"c1"});

        DetalleProfesional d = new ProfesionalUseCase(profesionales, especialidades, null, null, null, Dobles.TX, Dobles.IDS)
                .asociarEspecialidades(new ProfesionalUseCase.AsociarEspecialidadesComando("p1", new String[] {"e1", "e2", "e1"}));

        verify(profesionales).reemplazarEspecialidades("p1", new String[] {"e1", "e2"});
        assertArrayEquals(new String[] {"e1", "e2"}, d.especialidadIds());
        assertArrayEquals(new String[] {"c1"}, d.centroIds());
    }

    @Test
    void rechazaEspecialidadesInactivasInexistentesOProfesionalInexistente() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(especialidades.buscarPorId("inactiva")).thenReturn(new Especialidad("inactiva", "X", null, false));
        when(especialidades.buscarPorId("nada")).thenReturn(null);
        when(profesionales.buscarPorId("fantasma")).thenReturn(null);
        ProfesionalUseCase useCase = new ProfesionalUseCase(profesionales, especialidades, null, null, null, Dobles.TX, Dobles.IDS);

        assertEquals("ESPECIALIDAD_INACTIVA", assertThrows(ValidacionException.class,
                () -> useCase.asociarEspecialidades(new ProfesionalUseCase.AsociarEspecialidadesComando("p1", new String[] {"inactiva"}))).codigo());
        assertThrows(NoEncontradoException.class, () -> useCase.asociarEspecialidades(new ProfesionalUseCase.AsociarEspecialidadesComando("p1", new String[] {"nada"})));
        assertThrows(NoEncontradoException.class, () -> useCase.asociarEspecialidades(new ProfesionalUseCase.AsociarEspecialidadesComando("fantasma", new String[0])));
        verify(profesionales, never()).reemplazarEspecialidades(any(), any());
    }

    @Test
    void asociaCentrosActivos() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(new Centro("c1", "Sede", "Calle 1", "Cali", null, true));
        when(profesionales.idsCentros("p1")).thenReturn(new String[0]);
        when(profesionales.idsEspecialidades("p1")).thenReturn(new String[0]);

        new ProfesionalUseCase(profesionales, null, centros, horarios, bloqueos, Dobles.TX, Dobles.IDS).asociarCentros(new ProfesionalUseCase.AsociarCentrosComando("p1", new String[] {"c1"}));

        verify(profesionales).reemplazarCentros("p1", new String[] {"c1"});
    }

    @Test
    void noPermiteQuitarUnCentroQueTieneHorariosOBloqueos() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(profesionales.idsCentros("p1")).thenReturn(new String[] {"c1", "c2"});
        when(horarios.contarPorProfesionalYCentro("p1", "c1")).thenReturn(2);
        ProfesionalUseCase useCase = new ProfesionalUseCase(profesionales, null, centros, horarios, bloqueos, Dobles.TX, Dobles.IDS);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> useCase.asociarCentros(new ProfesionalUseCase.AsociarCentrosComando("p1", new String[0])));

        assertEquals("CENTRO_CON_HORARIOS", ex.codigo());
        verify(profesionales, never()).reemplazarCentros(any(), any());
    }

    @Test
    void quitarUnCentroSinHorariosNiBloqueosEsPosible() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(profesionales.idsCentros("p1")).thenReturn(new String[] {"c1"});
        when(horarios.contarPorProfesionalYCentro("p1", "c1")).thenReturn(0);
        when(bloqueos.contarPorProfesionalYCentro("p1", "c1")).thenReturn(0);
        when(profesionales.idsEspecialidades("p1")).thenReturn(new String[0]);

        new ProfesionalUseCase(profesionales, null, centros, horarios, bloqueos, Dobles.TX, Dobles.IDS).asociarCentros(new ProfesionalUseCase.AsociarCentrosComando("p1", new String[0]));

        verify(profesionales).reemplazarCentros("p1", new String[0]);
    }
}
