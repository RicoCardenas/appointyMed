package com.appointy.clinicalhistory.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.exception.NoEncontradoException;
import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.exception.ValidacionException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.Cie10ClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConsultasAdicionalesTest {

    static final Actor PROFESIONAL = new Actor("u-prof", "PROFESIONAL");
    static final Actor PACIENTE = new Actor("p1", "PACIENTE");

    final LogAccesoRepositoryGateway logs = mock(LogAccesoRepositoryGateway.class);
    final Auditoria auditoria = new Auditoria(logs, Dobles.reloj("2026-11-14T12:00:00Z"), Dobles.REGISTRO, Dobles.IDS);
    final CatalogClientGateway catalog = mock(CatalogClientGateway.class);
    final AppointmentClientGateway appointment = mock(AppointmentClientGateway.class);
    final EntradaClinicaRepositoryGateway entradas = mock(EntradaClinicaRepositoryGateway.class);

    static EntradaClinica entrada(String id, String paciente, String cita, String corrige, long creado, String diagnostico) {
        return new EntradaClinica(id, paciente, "prof1", cita, "motivo", diagnostico, null, null, corrige, creado);
    }

    @Test
    void buscarDiagnosticosExigeProfesionalYValidaElTermino() {
        Cie10ClientGateway cie10 = mock(Cie10ClientGateway.class);
        when(cie10.buscar("diabetes", ReglasHistoria.MAXIMO_DIAGNOSTICOS)).thenReturn(new Diagnostico[] {new Diagnostico("E11", "Diabetes")});
        HistoriaUseCase caso = new HistoriaUseCase(null, null, null, null, cie10);
        assertEquals("E11", caso.buscarDiagnosticos(PROFESIONAL, "  diabetes ")[0].codigo());
        assertThrows(AccesoProhibidoException.class, () -> caso.buscarDiagnosticos(PACIENTE, "diabetes"));
        assertThrows(ValidacionException.class, () -> caso.buscarDiagnosticos(PROFESIONAL, "a"));
        assertThrows(ValidacionException.class, () -> caso.buscarDiagnosticos(PROFESIONAL, null));
        assertThrows(ValidacionException.class, () -> caso.buscarDiagnosticos(PROFESIONAL, "x".repeat(81)));
        assertThrows(ValidacionException.class, () -> caso.buscarDiagnosticos(PROFESIONAL, "diabetes; drop"));
        verify(cie10, org.mockito.Mockito.times(1)).buscar(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void laApiExternaCaidaSePropagaComoServicioNoDisponible() {
        Cie10ClientGateway cie10 = mock(Cie10ClientGateway.class);
        when(cie10.buscar(any(), org.mockito.ArgumentMatchers.anyInt())).thenThrow(new ServicioNoDisponibleException("CIE10_NO_DISPONIBLE", "x"));
        assertThrows(ServicioNoDisponibleException.class, () -> new HistoriaUseCase(null, null, null, null, cie10).buscarDiagnosticos(PROFESIONAL, "gripe"));
    }

    @Test
    void elResumenCuentaAtencionesYCorrecciones() {
        EntradaClinica[] historia = {entrada("1", "p1", "c1", null, 100, "Gripe"), entrada("2", "p1", "c1", "1", 200, "Gripe A"),
                entrada("3", "p1", "c2", null, 300, "Fiebre")};
        permitirLecturaDelProfesional("p1");
        when(entradas.listarDePaciente("p1")).thenReturn(historia);
        HistoriaUseCase caso = new HistoriaUseCase(catalog, appointment, entradas, auditoria, null);

        ResumenHistoria r = caso.consultarResumenPaciente(PROFESIONAL, "p1");

        assertEquals(new ResumenHistoria(2, 1, 300, "Fiebre"), r);
    }

    @Test
    void elResumenPropioDeUnPacienteSinHistoriaEstaVacio() {
        when(entradas.listarDePaciente("p1")).thenReturn(new EntradaClinica[0]);

        ResumenHistoria r = new HistoriaUseCase(null, null, entradas, auditoria, null).consultarMiResumen(PACIENTE);

        assertEquals(new ResumenHistoria(0, 0, 0, null), r);
    }

    private void permitirLecturaDelProfesional(String pacienteId) {
        when(catalog.buscarProfesionalDeUsuario("u-prof")).thenReturn(new ProfesionalDeUsuario("prof1", true));
        when(appointment.existeRelacion("prof1", pacienteId)).thenReturn(true);
    }

    @Test
    void lasVersionesSonLasEntradasDeLaMismaCita() {
        when(entradas.buscarPorId("2")).thenReturn(entrada("2", "p1", "c1", "1", 200, "d"));
        permitirLecturaDelProfesional("p1");
        when(entradas.listarDePaciente("p1")).thenReturn(new EntradaClinica[] {entrada("1", "p1", "c1", null, 100, "d"),
                entrada("2", "p1", "c1", "1", 200, "d"), entrada("3", "p1", "c2", null, 300, "d")});
        EntradaClinicaUseCase caso = new EntradaClinicaUseCase(null, null, entradas, null, auditoria, null,
                new HistoriaUseCase(catalog, appointment, entradas, auditoria, null), Dobles.IDS);

        EntradaClinica[] versiones = caso.listarVersionesEntrada(PROFESIONAL, "2");

        assertEquals(2, versiones.length);
        assertEquals("1", versiones[0].id());
    }

    @Test
    void unaEntradaInexistenteSeRechazaYQuedaRegistrada() {
        EntradaClinicaUseCase caso = new EntradaClinicaUseCase(null, null, entradas, null, auditoria, null, null, Dobles.IDS);

        assertThrows(NoEncontradoException.class, () -> caso.listarVersionesEntrada(PROFESIONAL, "nada"));

        ArgumentCaptor<LogAcceso> log = ArgumentCaptor.forClass(LogAcceso.class);
        verify(logs).registrar(log.capture());
        assertEquals(ResultadoAcceso.DENEGADO, log.getValue().resultado());
        assertEquals("nada", log.getValue().entradaId());
    }

    @Test
    void listarAtencionesRegistraCadaPacienteLeido() {
        when(catalog.buscarProfesionalDeUsuario("u-prof")).thenReturn(new ProfesionalDeUsuario("prof1", true));
        when(entradas.listarDeProfesional("prof1", 0, 10)).thenReturn(new PaginaEntradas(new EntradaClinica[] {entrada("1", "p1", "c1", null, 1, "d"),
                entrada("2", "p2", "c2", null, 2, "d"), entrada("3", "p1", "c3", null, 3, "d")}, 0, 10, 3, 1));
        PaginaEntradas pagina = new HistoriaUseCase(catalog, null, entradas, auditoria, null).listarAtenciones(PROFESIONAL, 0, 10);
        assertEquals(3, pagina.totalElementos());
        ArgumentCaptor<LogAcceso> log = ArgumentCaptor.forClass(LogAcceso.class);
        verify(logs, org.mockito.Mockito.times(2)).registrar(log.capture());
        assertTrue(log.getAllValues().stream().allMatch(l -> l.resultado() == ResultadoAcceso.PERMITIDO && l.accion() == AccionAcceso.LECTURA));
        assertEquals("p1", log.getAllValues().get(0).pacienteId());
        assertEquals("p2", log.getAllValues().get(1).pacienteId());
    }

    @Test
    void listarAtencionesRechazaOtrosRolesPerfilesInactivosYPaginasInvalidas() {
        HistoriaUseCase caso = new HistoriaUseCase(catalog, null, entradas, auditoria, null);
        assertThrows(AccesoProhibidoException.class, () -> caso.listarAtenciones(PACIENTE, 0, 10));
        when(catalog.buscarProfesionalDeUsuario("u-prof")).thenReturn(null);
        assertThrows(AccesoProhibidoException.class, () -> caso.listarAtenciones(PROFESIONAL, 0, 10));
        assertThrows(ValidacionException.class, () -> caso.listarAtenciones(PROFESIONAL, -1, 10));
        assertThrows(ValidacionException.class, () -> caso.listarAtenciones(PROFESIONAL, 0, 101));
        verify(entradas, never()).listarDeProfesional(any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void reglasDeTerminoYPaginacion() {
        assertEquals("tos", ReglasHistoria.validarTermino(" tos "));
        assertEquals("e11.9", ReglasHistoria.validarTermino("e11.9"));
        ReglasHistoria.validarPaginacion(0, 1);
        assertNull(ReglasHistoria.resumir(new EntradaClinica[0]).ultimoDiagnostico());
    }
}
