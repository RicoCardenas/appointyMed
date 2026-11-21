package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.ConflictoException;
import com.appointy.appointment.domain.exception.NoEncontradoException;
import com.appointy.appointment.domain.exception.ReglaNegocioException;
import com.appointy.appointment.domain.exception.ServicioNoDisponibleException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;
import com.appointy.appointment.domain.usecase.CitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * Reloj fijo: 2030-06-10 10:00 en Bogota (15:00 UTC). Parametros: anticipacion 2 h, cancelacion 24 h, horizonte 60 dias,
 * es decir, hasta 2030-08-09.
 */
class CitasUseCasesTest {

    static final Actor PACIENTE = new Actor("pac", "PACIENTE");
    static final Actor ADMIN = new Actor("adm", "ADMINISTRATIVO");
    static final Actor DOCTOR = new Actor("usr-doc", "PROFESIONAL");
    static final ParametrosCita PARAMETROS = new ParametrosCita(2, 24, 60);

    FranjaRepositoryGateway franjas = mock(FranjaRepositoryGateway.class);
    CitaRepositoryGateway citas = mock(CitaRepositoryGateway.class);
    HistorialCitaRepositoryGateway historial = mock(HistorialCitaRepositoryGateway.class);
    PublicadorEventosGateway eventos = mock(PublicadorEventosGateway.class);
    CatalogClientGateway catalog = mock(CatalogClientGateway.class);
    RelojGateway reloj = Dobles.reloj("2030-06-10T15:00:00Z", "America/Bogota");

    CitaUseCase casos;

    @BeforeEach
    void preparar() {
        casos = new CitaUseCase(catalog,
                new ReservarCitaTransaccion(franjas, citas, historial, eventos, reloj, Dobles.TX, Dobles.IDS),
                new ReprogramarCitaTransaccion(franjas, citas, historial, eventos, reloj, Dobles.TX, Dobles.IDS),
                new CancelarCitaTransaccion(franjas, citas, historial, eventos, reloj, Dobles.TX, Dobles.IDS),
                new MarcarCitaAtendidaTransaccion(franjas, citas, historial, reloj, Dobles.TX, Dobles.IDS), citas, franjas);
        when(catalog.obtenerParametros()).thenReturn(PARAMETROS);
        when(catalog.validarAsociacion(anyString(), anyString(), anyString())).thenReturn(new AsociacionProfesional(true, true));
        when(franjas.franjasDeCitasConfirmadasDelPaciente(anyString(), anyString())).thenReturn(new Franja[0]);
        when(franjas.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(citas.guardar(any())).thenAnswer(i -> i.getArgument(0));
    }

    static Franja franja(String id, String fecha, int ini, EstadoFranja estado) {
        return new Franja(id, "prof", "centro", fecha, ini, ini + 30, estado, 1L);
    }

    static Cita cita(String franjaId, EstadoCita estado) {
        return new Cita("cita1", "pac", "prof", "esp", "centro", franjaId, estado, 1, 1, 1L);
    }

    CitaUseCase.ReservarCitaComando comandoReserva(String franjaId) {
        return new CitaUseCase.ReservarCitaComando(PACIENTE, "esp", "prof", "centro", franjaId);
    }

    // ---- CU-18

    @Test
    void reservaOcupaLaFranjaCreaLaCitaEscribeHistorialYPublicaElEvento() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(franja("f1", "2030-06-11", 480, EstadoFranja.DISPONIBLE));

        CitaConFranja r = casos.reservarCita(comandoReserva("f1"));

        assertEquals(EstadoCita.CONFIRMADA, r.cita().estado());
        assertEquals("pac", r.cita().pacienteId());
        assertEquals(EstadoFranja.OCUPADA, r.franja().estado());
        InOrder orden = inOrder(citas, franjas);
        orden.verify(citas).bloquearPaciente("pac");
        orden.verify(franjas).buscarParaActualizar("f1");
        ArgumentCaptor<HistorialCita> fila = ArgumentCaptor.forClass(HistorialCita.class);
        verify(historial).registrar(fila.capture());
        assertNull(fila.getValue().estadoAnterior());
        assertEquals(EstadoCita.CONFIRMADA, fila.getValue().estadoNuevo());
        assertEquals("f1", fila.getValue().franjaNuevaId());
        ArgumentCaptor<EventoCita> evento = ArgumentCaptor.forClass(EventoCita.class);
        verify(eventos).publicar(evento.capture());
        assertEquals("CONFIRMADA", evento.getValue().tipo());
        assertEquals("2030-06-11", evento.getValue().fecha());
    }

    @Test
    void reservaDeFranjaOcupadaEsConflictoYNoEscribeNada() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(franja("f1", "2030-06-11", 480, EstadoFranja.OCUPADA));

        assertEquals("FRANJA_NO_DISPONIBLE", assertThrows(ConflictoException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());

        verify(franjas, never()).guardar(any());
        verify(citas, never()).guardar(any());
        verifyNoInteractions(historial, eventos);
    }

    @Test
    void reservaDeFranjaInexistenteEs404() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(null);
        assertThrows(NoEncontradoException.class, () -> casos.reservarCita(comandoReserva("f1")));
    }

    @Test
    void reservaConProfesionalNoAsociadoEs422YNoTocaLaBase() {
        when(catalog.validarAsociacion("prof", "esp", "centro")).thenReturn(new AsociacionProfesional(true, false));

        assertEquals("PROFESIONAL_NO_ASOCIADO", assertThrows(ReglaNegocioException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());

        verifyNoInteractions(franjas, citas, historial, eventos);
    }

    @Test
    void siCatalogNoRespondeHayUn503SinCambios() {
        when(catalog.obtenerParametros()).thenThrow(new ServicioNoDisponibleException("CATALOG_NO_DISPONIBLE", "caido"));

        assertThrows(ServicioNoDisponibleException.class, () -> casos.reservarCita(comandoReserva("f1")));

        verifyNoInteractions(franjas, citas, historial, eventos);
    }

    @Test
    void reservaConCruceConOtraCitaDelPacienteEsConflicto() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(franja("f1", "2030-06-11", 480, EstadoFranja.DISPONIBLE));
        when(franjas.franjasDeCitasConfirmadasDelPaciente("pac", "2030-06-11")).thenReturn(new Franja[] {franja("f9", "2030-06-11", 490, EstadoFranja.OCUPADA)});

        assertEquals("CRUCE_DE_CITAS", assertThrows(ConflictoException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());

        verify(citas, never()).guardar(any());
    }

    @Test
    void reservaConMenosAnticipacionQueLaMinimaEs422() {
        // Ahora 10:00; la franja de las 11:30 queda a 90 minutos, menos de 2 horas.
        when(franjas.buscarParaActualizar("f1")).thenReturn(franja("f1", "2030-06-10", 690, EstadoFranja.DISPONIBLE));
        assertEquals("ANTICIPACION_INSUFICIENTE", assertThrows(ReglaNegocioException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());
    }

    @Test
    void reservaMasAllaDelHorizonteEs422() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(franja("f1", "2030-08-10", 480, EstadoFranja.DISPONIBLE));
        assertEquals("FUERA_DEL_HORIZONTE", assertThrows(ReglaNegocioException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());
    }

    @Test
    void reservaDeFranjaDeOtroProfesionalEs422() {
        when(franjas.buscarParaActualizar("f1")).thenReturn(new Franja("f1", "otro", "centro", "2030-06-11", 480, 510, EstadoFranja.DISPONIBLE, 1L));
        assertEquals("FRANJA_NO_COINCIDE", assertThrows(ReglaNegocioException.class, () -> casos.reservarCita(comandoReserva("f1"))).codigo());
    }

    // ---- CU-19

    @Test
    void reprogramarOcupaLaNuevaLiberaLaAnteriorYBloqueaEnOrdenDeId() {
        Franja anterior = franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA);
        Franja nueva = franja("f2", "2030-06-13", 540, EstadoFranja.DISPONIBLE);
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarPorId("f2")).thenReturn(nueva);
        when(franjas.buscarParaActualizar("f9")).thenReturn(anterior);
        when(franjas.buscarParaActualizar("f2")).thenReturn(nueva);
        when(franjas.franjasDeCitasConfirmadasDelPaciente("pac", "2030-06-13")).thenReturn(new Franja[0]);

        CitaConFranja r = casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f2"));

        assertEquals("f2", r.cita().franjaId());
        assertEquals(EstadoCita.CONFIRMADA, r.cita().estado());
        InOrder orden = inOrder(franjas);
        orden.verify(franjas).buscarParaActualizar("f2");  // "f2" < "f9": se bloquea primero la de id menor
        orden.verify(franjas).buscarParaActualizar("f9");
        ArgumentCaptor<Franja> guardadas = ArgumentCaptor.forClass(Franja.class);
        verify(franjas, org.mockito.Mockito.times(2)).guardar(guardadas.capture());
        assertTrue(guardadas.getAllValues().stream().anyMatch(f -> f.id().equals("f2") && f.estado() == EstadoFranja.OCUPADA));
        assertTrue(guardadas.getAllValues().stream().anyMatch(f -> f.id().equals("f9") && f.estado() == EstadoFranja.DISPONIBLE));
        ArgumentCaptor<HistorialCita> fila = ArgumentCaptor.forClass(HistorialCita.class);
        verify(historial).registrar(fila.capture());
        assertEquals("f9", fila.getValue().franjaAnteriorId());
        assertEquals("f2", fila.getValue().franjaNuevaId());
        verify(eventos).publicar(any(EventoCita.class));
    }

    @Test
    void reprogramarAUnaFranjaOcupadaDejaLaCitaIntacta() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        Franja ocupada = franja("f2", "2030-06-13", 540, EstadoFranja.OCUPADA);
        when(franjas.buscarPorId("f2")).thenReturn(ocupada);
        when(franjas.buscarParaActualizar("f2")).thenReturn(ocupada);
        when(franjas.buscarParaActualizar("f9")).thenReturn(franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA));

        assertEquals("FRANJA_NO_DISPONIBLE", assertThrows(ConflictoException.class,
                () -> casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f2"))).codigo());

        verify(franjas, never()).guardar(any());
        verify(citas, never()).guardar(any());
        verifyNoInteractions(historial, eventos);
    }

    @Test
    void reprogramarCitaCancelada_oCompletada_esConflicto() {
        for (EstadoCita e : new EstadoCita[] {EstadoCita.CANCELADA, EstadoCita.COMPLETADA}) {
            when(citas.buscarPorId("cita1")).thenReturn(cita("f9", e));
            assertEquals("CITA_NO_MODIFICABLE", assertThrows(ConflictoException.class,
                    () -> casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f2"))).codigo());
        }
        verify(franjas, never()).guardar(any());
    }

    @Test
    void reprogramarCitaAjenaEs403() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        assertThrows(AccesoProhibidoException.class,
                () -> casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(new Actor("otro", "PACIENTE"), "cita1", "f2")));
    }

    @Test
    void reprogramarSinAnticipacionSobreLaCitaActualEs422() {
        // La cita actual es hoy a las 11:00 (a 60 minutos): ya no se puede mover.
        Franja anterior = franja("f9", "2030-06-10", 660, EstadoFranja.OCUPADA);
        Franja nueva = franja("f2", "2030-06-13", 540, EstadoFranja.DISPONIBLE);
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarPorId("f2")).thenReturn(nueva);
        when(franjas.buscarParaActualizar("f9")).thenReturn(anterior);
        when(franjas.buscarParaActualizar("f2")).thenReturn(nueva);

        assertEquals("ANTICIPACION_INSUFICIENTE", assertThrows(ReglaNegocioException.class,
                () -> casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f2"))).codigo());
    }

    @Test
    void reprogramarALaMismaFranjaEs422() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarPorId("f9")).thenReturn(franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA));
        assertEquals("MISMA_FRANJA", assertThrows(ReglaNegocioException.class,
                () -> casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f9"))).codigo());
    }

    @Test
    void reprogramarPuedeEncimarseConLaPropiaCitaQueSeLibera() {
        // La nueva franja se cruza solo con la franja que la cita deja libre: no es cruce.
        Franja anterior = franja("f9", "2030-06-13", 540, EstadoFranja.OCUPADA);
        Franja nueva = franja("f2", "2030-06-13", 555, EstadoFranja.DISPONIBLE);
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarPorId("f2")).thenReturn(nueva);
        when(franjas.buscarParaActualizar("f9")).thenReturn(anterior);
        when(franjas.buscarParaActualizar("f2")).thenReturn(nueva);
        when(franjas.franjasDeCitasConfirmadasDelPaciente("pac", "2030-06-13")).thenReturn(new Franja[] {anterior});

        assertEquals("f2", casos.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(PACIENTE, "cita1", "f2")).cita().franjaId());
    }

    // ---- CU-20

    @Test
    void cancelarLiberaLaFranjaYRegistraQuienCancelo() {
        Franja ocupada = franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA);
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarParaActualizar("f9")).thenReturn(ocupada);

        CitaConFranja r = casos.cancelarCita(new CitaUseCase.CancelarCitaComando(ADMIN, "cita1", "paciente enfermo"));

        assertEquals(EstadoCita.CANCELADA, r.cita().estado());
        assertEquals(EstadoFranja.DISPONIBLE, r.franja().estado());
        ArgumentCaptor<HistorialCita> fila = ArgumentCaptor.forClass(HistorialCita.class);
        verify(historial).registrar(fila.capture());
        assertEquals("adm", fila.getValue().usuarioId());
        assertEquals("ADMINISTRATIVO", fila.getValue().rolUsuario());
        assertEquals(EstadoCita.CONFIRMADA, fila.getValue().estadoAnterior());
        assertEquals(EstadoCita.CANCELADA, fila.getValue().estadoNuevo());
        ArgumentCaptor<EventoCita> evento = ArgumentCaptor.forClass(EventoCita.class);
        verify(eventos).publicar(evento.capture());
        assertEquals("CANCELADA", evento.getValue().tipo());
        assertEquals("pac", evento.getValue().pacienteId());
    }

    @Test
    void cancelarFueraDelPlazoEs422() {
        // Ahora 10:00 del 10; la cita es el 11 a las 08:00, a 22 horas, y el limite son 24.
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarParaActualizar("f9")).thenReturn(franja("f9", "2030-06-11", 480, EstadoFranja.OCUPADA));

        assertEquals("FUERA_DEL_PLAZO_DE_CANCELACION", assertThrows(ReglaNegocioException.class,
                () -> casos.cancelarCita(new CitaUseCase.CancelarCitaComando(PACIENTE, "cita1", null))).codigo());

        verify(citas, never()).guardar(any());
        verifyNoInteractions(historial, eventos);
    }

    @Test
    void cancelarCitaYaCanceladaOCompletadaEsConflicto() {
        for (EstadoCita e : new EstadoCita[] {EstadoCita.CANCELADA, EstadoCita.COMPLETADA}) {
            when(citas.buscarPorId("cita1")).thenReturn(cita("f9", e));
            assertEquals("CITA_NO_MODIFICABLE", assertThrows(ConflictoException.class,
                    () -> casos.cancelarCita(new CitaUseCase.CancelarCitaComando(PACIENTE, "cita1", null))).codigo());
        }
    }

    @Test
    void cancelarCitaAjenaEs403() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        assertThrows(AccesoProhibidoException.class, () -> casos.cancelarCita(new CitaUseCase.CancelarCitaComando(new Actor("otro", "PACIENTE"), "cita1", null)));
    }

    @Test
    void siLaCitaSeMovioEntreLaLecturaYElBloqueoSeRechazaComoConflicto() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA)).thenReturn(cita("f7", EstadoCita.CONFIRMADA));
        when(franjas.buscarParaActualizar("f9")).thenReturn(franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA));

        assertEquals("CITA_CAMBIO", assertThrows(ConflictoException.class,
                () -> casos.cancelarCita(new CitaUseCase.CancelarCitaComando(PACIENTE, "cita1", null))).codigo());

        verify(franjas, never()).guardar(any());
    }

    // ---- CU-23

    @Test
    void elProfesionalMarcaSuCitaCompletadaYLaFranjaSigueOcupada() {
        when(catalog.buscarProfesionalDeUsuario("usr-doc")).thenReturn(new ProfesionalDeUsuario("prof", true));
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(franjas.buscarParaActualizar("f9")).thenReturn(franja("f9", "2030-06-12", 480, EstadoFranja.OCUPADA));

        CitaConFranja r = casos.marcarCitaAtendida(DOCTOR, "cita1", EstadoCita.COMPLETADA);

        assertEquals(EstadoCita.COMPLETADA, r.cita().estado());
        verify(franjas, never()).guardar(any());
        verify(historial).registrar(any(HistorialCita.class));
    }

    @Test
    void marcarCitaDeOtroProfesionalEs403() {
        when(catalog.buscarProfesionalDeUsuario("usr-doc")).thenReturn(new ProfesionalDeUsuario("otro", true));
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        assertThrows(AccesoProhibidoException.class, () -> casos.marcarCitaAtendida(DOCTOR, "cita1", EstadoCita.NO_ASISTIDA));
    }

    @Test
    void marcarSinPerfilDeProfesionalEs403YEstadoInvalidoEs400() {
        when(catalog.buscarProfesionalDeUsuario("usr-doc")).thenReturn(null);
        assertThrows(AccesoProhibidoException.class, () -> casos.marcarCitaAtendida(DOCTOR, "cita1", EstadoCita.COMPLETADA));
        assertThrows(ValidacionException.class, () -> casos.marcarCitaAtendida(DOCTOR, "cita1", EstadoCita.CANCELADA));
    }

    // ---- CU-21, CU-22, CU-24

    @Test
    void misCitasProximasYPasadasUsanElHoyDeLaClinica() {
        Cita c1 = cita("f1", EstadoCita.CONFIRMADA);
        Cita c2 = new Cita("cita2", "pac", "prof", "esp", "centro", "f2", EstadoCita.CONFIRMADA, 1, 1, 1L);
        when(citas.listarDePaciente("pac", "2030-06-10", FechaIso.FECHA_MAXIMA)).thenReturn(new Cita[] {c1, c2});
        when(citas.listarDePaciente("pac", FechaIso.FECHA_MINIMA, "2030-06-09")).thenReturn(new Cita[] {c1, c2});
        when(franjas.buscarPorIds(any())).thenReturn(new Franja[] {franja("f1", "2030-06-11", 480, EstadoFranja.OCUPADA), franja("f2", "2030-06-12", 480, EstadoFranja.OCUPADA)});
        ConsultaCitaUseCase uc = new ConsultaCitaUseCase(null, citas, franjas, reloj, null, Dobles.TX);

        assertEquals("cita1", uc.consultarMisCitas("pac", "PROXIMAS")[0].cita().id());
        assertEquals("cita2", uc.consultarMisCitas("pac", "PASADAS")[0].cita().id());
        assertThrows(ValidacionException.class, () -> uc.consultarMisCitas("pac", "otro"));
    }

    @Test
    void agendaSinFechaVaDeHoyEnAdelanteYConFechaSoloEseDia() {
        when(catalog.buscarProfesionalDeUsuario("usr-doc")).thenReturn(new ProfesionalDeUsuario("prof", true));
        when(citas.listarAgendaConfirmada(anyString(), anyString(), anyString())).thenReturn(new Cita[0]);
        when(franjas.buscarPorIds(any())).thenReturn(new Franja[0]);
        ConsultaCitaUseCase uc = new ConsultaCitaUseCase(catalog, citas, franjas, reloj, null, Dobles.TX);

        uc.consultarAgenda("usr-doc", null);
        verify(citas).listarAgendaConfirmada("prof", "2030-06-10", FechaIso.FECHA_MAXIMA);
        uc.consultarAgenda("usr-doc", "2030-06-15");
        verify(citas).listarAgendaConfirmada("prof", "2030-06-15", "2030-06-15");
    }

    @Test
    void historialSoloParaSuPacienteSuProfesionalOAdministrativo() {
        when(citas.buscarPorId("cita1")).thenReturn(cita("f9", EstadoCita.CONFIRMADA));
        when(historial.listarPorCita("cita1")).thenReturn(new HistorialCita[0]);
        when(catalog.buscarProfesionalDeUsuario("usr-doc")).thenReturn(new ProfesionalDeUsuario("prof", true));
        when(catalog.buscarProfesionalDeUsuario("usr-otro")).thenReturn(new ProfesionalDeUsuario("otro", true));
        ConsultaCitaUseCase uc = new ConsultaCitaUseCase(catalog, citas, null, null, historial, Dobles.TX);

        assertArrayEquals(new HistorialCita[0], uc.consultarHistorialCita(PACIENTE, "cita1"));
        uc.consultarHistorialCita(ADMIN, "cita1");
        uc.consultarHistorialCita(DOCTOR, "cita1");
        assertThrows(AccesoProhibidoException.class, () -> uc.consultarHistorialCita(new Actor("otro", "PACIENTE"), "cita1"));
        assertThrows(AccesoProhibidoException.class, () -> uc.consultarHistorialCita(new Actor("usr-otro", "PROFESIONAL"), "cita1"));
    }

    @Test
    void disponibilidadOmiteLasFranjasQueYaPasaron() {
        // Ahora 10:00 (600 min): la de 09:00 ya paso, la de 10:00 esta empezando y la de 10:30 es futura.
        when(franjas.listarDisponibles("prof", "centro", "2030-06-10")).thenReturn(new Franja[] {
            franja("a", "2030-06-10", 540, EstadoFranja.DISPONIBLE), franja("b", "2030-06-10", 600, EstadoFranja.DISPONIBLE),
            franja("c", "2030-06-10", 630, EstadoFranja.DISPONIBLE)});

        Franja[] r = new FranjaUseCase(franjas, null, reloj, Dobles.TX, Dobles.IDS).consultarDisponibilidad("prof", "centro", "2030-06-10");

        assertEquals(1, r.length);
        assertEquals("c", r[0].id());
    }
}
