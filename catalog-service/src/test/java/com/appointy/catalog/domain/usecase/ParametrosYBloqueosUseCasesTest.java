package com.appointy.catalog.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoCitasException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.HorarioUseCase;
import com.appointy.catalog.domain.usecase.ParametroUseCase;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.gateway.RelojGateway;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParametrosYBloqueosUseCasesTest {

    @Mock ParametroRepositoryGateway parametros;
    @Mock ProfesionalRepositoryGateway profesionales;
    @Mock CentroRepositoryGateway centros;
    @Mock BloqueoRepositoryGateway bloqueos;
    @Mock AppointmentClientGateway appointment;

    final RelojGateway reloj = Dobles.reloj("2026-06-01T10:00:00Z");
    final Profesional ana = new Profesional("p1", "u1", "Ana", "Perez", "RM-1", true);
    final Centro sede = new Centro("c1", "Sede", "Calle 1", "Cali", null, true);

    private ParametroServicio p(String clave, String valor) {
        return new ParametroServicio(clave, valor, null);
    }

    // ---- CU-15 ----
    @Test
    void configurarGuardaLosValoresConservandoLaDescripcion() {
        when(parametros.buscarPorClave("horizonte_max_dias")).thenReturn(new ParametroServicio("horizonte_max_dias", "60", "Dias"));
        when(parametros.listar()).thenReturn(new ParametroServicio[] {p("horizonte_max_dias", "45")});

        new ParametroUseCase(parametros, null, Dobles.TX).configurarParametros(new ParametroUseCase.ConfigurarParametrosComando(new ParametroServicio[] {p("horizonte_max_dias", " 45 ")}));

        verify(parametros).guardar(new ParametroServicio("horizonte_max_dias", "45", "Dias"));
    }

    @Test
    void configurarEsTodoONadaSiUnValorEsInvalidoNoSeGuardaNinguno() {
        ParametroUseCase useCase = new ParametroUseCase(parametros, null, Dobles.TX);

        assertThrows(ValidacionException.class, () -> useCase.configurarParametros(new ParametroUseCase.ConfigurarParametrosComando(
                new ParametroServicio[] {p("horizonte_max_dias", "45"), p("duracion_franja_min", "1")})));
        assertThrows(ValidacionException.class, () -> useCase.configurarParametros(new ParametroUseCase.ConfigurarParametrosComando(new ParametroServicio[0])));
        verify(parametros, never()).guardar(any());
    }

    // ---- CU-16 ----
    private HorarioUseCase bloquear() {
        return new HorarioUseCase(profesionales, centros, null, null, null, bloqueos, appointment, reloj, Dobles.TX, Dobles.IDS);
    }

    private HorarioUseCase.BloquearFranjasComando comando() {
        return new HorarioUseCase.BloquearFranjasComando("admin-1", "p1", "c1", "2026-07-01", "2026-07-15", " Vacaciones ");
    }

    private void profesionalYCentro() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(true);
    }

    @Test
    void bloquearGuardaElBloqueoYLuegoPideBloquearLasFranjas() {
        profesionalYCentro();
        when(bloqueos.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Bloqueo b = bloquear().bloquearFranjas(comando());

        assertEquals("Vacaciones", b.motivo());
        assertEquals("admin-1", b.creadoPor());
        assertEquals(reloj.ahora(), b.creadoEn());
        InOrder orden = inOrder(bloqueos, appointment);
        orden.verify(bloqueos).guardar(b);
        orden.verify(appointment).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-15");
    }

    @Test
    void siHayCitasConfirmadasLaExcepcionSubeParaRevertirElBloqueo() {
        profesionalYCentro();
        when(bloqueos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new ConflictoCitasException(new String[] {"cita-1", "cita-2"})).when(appointment).bloquearFranjas(any(), any(), any(), any());

        ConflictoCitasException ex = assertThrows(ConflictoCitasException.class, () -> bloquear().bloquearFranjas(comando()));

        assertEquals(2, ex.citasAfectadas().length);
        assertEquals("cita-1", ex.detalle()[0]);
    }

    @Test
    void siAppointmentNoRespondeLaExcepcionSubeParaRevertirElBloqueo() {
        profesionalYCentro();
        when(bloqueos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido")).when(appointment).bloquearFranjas(any(), any(), any(), any());

        assertThrows(ServicioNoDisponibleException.class, () -> bloquear().bloquearFranjas(comando()));
    }

    @Test
    void bloquearValidaElRangoElProfesionalYElCentroAntesDeTocarNada() {
        assertThrows(ValidacionException.class, () -> bloquear().bloquearFranjas(new HorarioUseCase.BloquearFranjasComando("a", "p1", "c1", "2026-07-15", "2026-07-01", "x")));
        verifyNoInteractions(profesionales, bloqueos, appointment);

        when(profesionales.buscarPorId("p1")).thenReturn(null);
        assertThrows(NoEncontradoException.class, () -> bloquear().bloquearFranjas(comando()));

        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(false);
        assertEquals("PROFESIONAL_NO_ATIENDE_EN_CENTRO", assertThrows(ValidacionException.class, () -> bloquear().bloquearFranjas(comando())).codigo());
        verify(bloqueos, never()).guardar(any());
        verify(appointment, never()).bloquearFranjas(any(), any(), any(), any());
    }

    // ---- consulta interna ----
    @Test
    void validarAsociacionRequiereEspecialidadYCentro() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(profesionales.tieneEspecialidad("p1", "e1")).thenReturn(true);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(true);
        when(profesionales.tieneEspecialidad("p1", "e2")).thenReturn(false);
        when(profesionales.buscarPorId("nada")).thenReturn(null);
        ProfesionalUseCase useCase = new ProfesionalUseCase(profesionales, null, null, null, null, Dobles.TX, Dobles.IDS);

        assertEquals(new ProfesionalUseCase.ValidarAsociacionResultado(true, true), useCase.validarAsociacion("p1", "e1", "c1"));
        assertEquals(new ProfesionalUseCase.ValidarAsociacionResultado(true, false), useCase.validarAsociacion("p1", "e2", "c1"));
        assertEquals(new ProfesionalUseCase.ValidarAsociacionResultado(false, false), useCase.validarAsociacion("nada", "e1", "c1"));
    }
}
