package com.appointy.catalog.application.usecase;

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
import com.appointy.catalog.domain.port.in.BloquearFranjasPort;
import com.appointy.catalog.domain.port.in.ConfigurarParametrosPort;
import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;
import com.appointy.catalog.domain.port.out.AppointmentClientPort;
import com.appointy.catalog.domain.port.out.BloqueoRepositoryPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParametrosYBloqueosUseCasesTest {

    @Mock ParametroRepositoryPort parametros;
    @Mock ProfesionalRepositoryPort profesionales;
    @Mock CentroRepositoryPort centros;
    @Mock BloqueoRepositoryPort bloqueos;
    @Mock AppointmentClientPort appointment;

    final Clock reloj = Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC);
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

        new ConfigurarParametrosUseCase(parametros).ejecutar(new ConfigurarParametrosPort.Comando(new ParametroServicio[] {p("horizonte_max_dias", " 45 ")}));

        verify(parametros).guardar(new ParametroServicio("horizonte_max_dias", "45", "Dias"));
    }

    @Test
    void configurarEsTodoONadaSiUnValorEsInvalidoNoSeGuardaNinguno() {
        ConfigurarParametrosUseCase useCase = new ConfigurarParametrosUseCase(parametros);

        assertThrows(ValidacionException.class, () -> useCase.ejecutar(new ConfigurarParametrosPort.Comando(
                new ParametroServicio[] {p("horizonte_max_dias", "45"), p("duracion_franja_min", "1")})));
        assertThrows(ValidacionException.class, () -> useCase.ejecutar(new ConfigurarParametrosPort.Comando(new ParametroServicio[0])));
        verify(parametros, never()).guardar(any());
    }

    // ---- CU-16 ----
    private BloquearFranjasUseCase bloquear() {
        return new BloquearFranjasUseCase(profesionales, centros, bloqueos, appointment, reloj);
    }

    private BloquearFranjasPort.Comando comando() {
        return new BloquearFranjasPort.Comando("admin-1", "p1", "c1", "2026-07-01", "2026-07-15", " Vacaciones ");
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

        Bloqueo b = bloquear().ejecutar(comando());

        assertEquals("Vacaciones", b.motivo());
        assertEquals("admin-1", b.creadoPor());
        assertEquals(reloj.millis(), b.creadoEn());
        InOrder orden = inOrder(bloqueos, appointment);
        orden.verify(bloqueos).guardar(b);
        orden.verify(appointment).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-15");
    }

    @Test
    void siHayCitasConfirmadasLaExcepcionSubeParaRevertirElBloqueo() {
        profesionalYCentro();
        when(bloqueos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new ConflictoCitasException(new String[] {"cita-1", "cita-2"})).when(appointment).bloquearFranjas(any(), any(), any(), any());

        ConflictoCitasException ex = assertThrows(ConflictoCitasException.class, () -> bloquear().ejecutar(comando()));

        assertEquals(2, ex.citasAfectadas().length);
        assertEquals("cita-1", ex.detalle()[0]);
    }

    @Test
    void siAppointmentNoRespondeLaExcepcionSubeParaRevertirElBloqueo() {
        profesionalYCentro();
        when(bloqueos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido")).when(appointment).bloquearFranjas(any(), any(), any(), any());

        assertThrows(ServicioNoDisponibleException.class, () -> bloquear().ejecutar(comando()));
    }

    @Test
    void bloquearValidaElRangoElProfesionalYElCentroAntesDeTocarNada() {
        assertThrows(ValidacionException.class, () -> bloquear().ejecutar(new BloquearFranjasPort.Comando("a", "p1", "c1", "2026-07-15", "2026-07-01", "x")));
        verifyNoInteractions(profesionales, bloqueos, appointment);

        when(profesionales.buscarPorId("p1")).thenReturn(null);
        assertThrows(NoEncontradoException.class, () -> bloquear().ejecutar(comando()));

        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(false);
        assertEquals("PROFESIONAL_NO_ATIENDE_EN_CENTRO", assertThrows(ValidacionException.class, () -> bloquear().ejecutar(comando())).codigo());
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
        ValidarAsociacionUseCase useCase = new ValidarAsociacionUseCase(profesionales);

        assertEquals(new ValidarAsociacionPort.Resultado(true, true), useCase.ejecutar("p1", "e1", "c1"));
        assertEquals(new ValidarAsociacionPort.Resultado(true, false), useCase.ejecutar("p1", "e2", "c1"));
        assertEquals(new ValidarAsociacionPort.Resultado(false, false), useCase.ejecutar("nada", "e1", "c1"));
    }
}
