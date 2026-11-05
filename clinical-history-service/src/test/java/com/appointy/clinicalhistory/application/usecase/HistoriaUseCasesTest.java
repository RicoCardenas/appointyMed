package com.appointy.clinicalhistory.application.usecase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.exception.ConflictoException;
import com.appointy.clinicalhistory.domain.exception.NoEncontradoException;
import com.appointy.clinicalhistory.domain.exception.ReglaNegocioException;
import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.exception.ValidacionException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.CitaClinica;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.EstadoCita;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import com.appointy.clinicalhistory.domain.usecase.CorregirEntradaUseCase;
import com.appointy.clinicalhistory.domain.usecase.RegistrarAtencionUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HistoriaUseCasesTest {

    static final Actor DOCTOR = new Actor("u-doc", "PROFESIONAL");
    static final Actor PACIENTE = new Actor("pac-1", "PACIENTE");
    static final ContenidoClinico TEXTO = new ContenidoClinico("  dolor de cabeza ", "migrana", "nota", "reposo");

    CatalogClientGateway catalog = mock(CatalogClientGateway.class);
    AppointmentClientGateway appointment = mock(AppointmentClientGateway.class);
    EntradaClinicaRepositoryGateway entradas = mock(EntradaClinicaRepositoryGateway.class);
    LogAccesoRepositoryGateway logs = mock(LogAccesoRepositoryGateway.class);
    Clock reloj = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
    Auditoria auditoria = new Auditoria(logs, reloj);
    GuardarEntradaTransaccion transaccion = new GuardarEntradaTransaccion(entradas, logs, auditoria);

    RegistrarAtencionUseCaseImpl registrar = new RegistrarAtencionUseCaseImpl(catalog, appointment, entradas, transaccion, auditoria, reloj);
    CorregirEntradaUseCaseImpl corregir = new CorregirEntradaUseCaseImpl(catalog, entradas, transaccion, auditoria, reloj);
    ConsultarHistoriaPacienteUseCaseImpl historiaDePaciente = new ConsultarHistoriaPacienteUseCaseImpl(catalog, appointment, entradas, auditoria);
    ConsultarMiHistoriaUseCaseImpl miHistoria = new ConsultarMiHistoriaUseCaseImpl(entradas, auditoria);

    void doctorConPerfil() {
        when(catalog.buscarProfesionalDeUsuario("u-doc")).thenReturn(new ProfesionalDeUsuario("prof-1", true));
    }

    LogAcceso ultimoLog(int veces) {
        ArgumentCaptor<LogAcceso> c = ArgumentCaptor.forClass(LogAcceso.class);
        verify(logs, times(veces)).registrar(c.capture());
        return c.getAllValues().get(veces - 1);
    }

    RegistrarAtencionUseCase.Comando atencion(Actor actor) {
        return new RegistrarAtencionUseCase.Comando(actor, "cita-1", TEXTO);
    }

    @Test
    void registrarGuardaLaEntradaYSuLogPermitidoJuntos() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "prof-1", EstadoCita.CONFIRMADA));

        EntradaClinica e = registrar.ejecutar(atencion(DOCTOR));

        assertEquals("pac-1", e.pacienteId());
        assertEquals("prof-1", e.profesionalId());
        assertEquals("dolor de cabeza", e.motivo());
        assertNull(e.corrigeAId());
        assertEquals(1_700_000_000_000L, e.creadoEn());
        verify(entradas).registrar(e);
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.PERMITIDO, log.resultado());
        assertEquals(AccionAcceso.ESCRITURA, log.accion());
        assertEquals(e.id(), log.entradaId());
        assertEquals("u-doc", log.usuarioId());
    }

    @Test
    void registrarAceptaUnaCitaCompletada() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "prof-1", EstadoCita.COMPLETADA));
        registrar.ejecutar(atencion(DOCTOR));
        verify(entradas).registrar(any());
    }

    @Test
    void registrarRechazaAQuienNoEsProfesionalYLoDejaEnElLog() {
        AccesoProhibidoException e = assertThrows(AccesoProhibidoException.class, () -> registrar.ejecutar(atencion(PACIENTE)));
        assertEquals("SOLO_PROFESIONAL", e.codigo());
        verify(entradas, never()).registrar(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.DENEGADO, log.resultado());
        assertEquals("PACIENTE", log.rol());
    }

    @Test
    void registrarRechazaSiLaCitaEsDeOtroProfesionalYRegistraElPaciente() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "otro-prof", EstadoCita.CONFIRMADA));
        assertEquals("NO_ES_TU_CITA", assertThrows(AccesoProhibidoException.class, () -> registrar.ejecutar(atencion(DOCTOR))).codigo());
        verify(entradas, never()).registrar(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.DENEGADO, log.resultado());
        assertEquals("pac-1", log.pacienteId());
    }

    @Test
    void registrarRechazaCitasCanceladasONoAsistidas() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "prof-1", EstadoCita.CANCELADA));
        assertEquals("CITA_NO_ATENDIBLE", assertThrows(ReglaNegocioException.class, () -> registrar.ejecutar(atencion(DOCTOR))).codigo());
        verify(entradas, never()).registrar(any());
    }

    @Test
    void registrarRechazaUnaSegundaEntradaOriginalDeLaMismaCita() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "prof-1", EstadoCita.CONFIRMADA));
        when(entradas.existeOriginalDeCita("cita-1")).thenReturn(true);
        assertEquals("ATENCION_YA_REGISTRADA", assertThrows(ConflictoException.class, () -> registrar.ejecutar(atencion(DOCTOR))).codigo());
        verify(entradas, never()).registrar(any());
    }

    @Test
    void registrarConCitaInexistenteEs404() {
        doctorConPerfil();
        assertEquals("CITA_NO_ENCONTRADA", assertThrows(NoEncontradoException.class, () -> registrar.ejecutar(atencion(DOCTOR))).codigo());
    }

    @Test
    void registrarConAppointmentCaidoEs503YNoGuardaNada() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido"));
        assertThrows(ServicioNoDisponibleException.class, () -> registrar.ejecutar(atencion(DOCTOR)));
        verify(entradas, never()).registrar(any());
        assertEquals(ResultadoAcceso.DENEGADO, ultimoLog(1).resultado());
    }

    @Test
    void registrarValidaElContenidoAntesDeLlamarAOtrosServicios() {
        assertThrows(ValidacionException.class, () -> registrar.ejecutar(
                new RegistrarAtencionUseCase.Comando(DOCTOR, "cita-1", new ContenidoClinico("", "d", null, null))));
        verify(appointment, never()).buscarCita(any());
    }

    @Test
    void siFallaElLogDeUnRechazoSeConservaElErrorOriginal() {
        doThrow(new IllegalStateException("bd caida")).when(logs).registrar(any());
        assertThrows(AccesoProhibidoException.class, () -> registrar.ejecutar(atencion(PACIENTE)));
    }

    @Test
    void siFallaElLogDeUnaEscrituraTodaLaTransaccionFalla() {
        doctorConPerfil();
        when(appointment.buscarCita("cita-1")).thenReturn(new CitaClinica("cita-1", "pac-1", "prof-1", EstadoCita.CONFIRMADA));
        doThrow(new IllegalStateException("bd caida")).when(logs).registrar(any());
        assertThrows(IllegalStateException.class, () -> registrar.ejecutar(atencion(DOCTOR)));
    }

    // ---- CU-31

    EntradaClinica original() {
        return new EntradaClinica("e-1", "pac-1", "prof-1", "cita-1", "m", "d", null, null, null, 1L);
    }

    @Test
    void corregirCreaUnaEntradaNuevaQueReferenciaALaAnteriorSinTocarla() {
        doctorConPerfil();
        when(entradas.buscarPorId("e-1")).thenReturn(original());

        EntradaClinica nueva = corregir.ejecutar(new CorregirEntradaUseCase.Comando(DOCTOR, "e-1", TEXTO));

        assertEquals("e-1", nueva.corrigeAId());
        assertEquals("pac-1", nueva.pacienteId());
        assertEquals("cita-1", nueva.citaId());
        assertEquals("prof-1", nueva.profesionalId());
        verify(entradas, times(1)).registrar(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(nueva.id(), log.entradaId());
        assertEquals(ResultadoAcceso.PERMITIDO, log.resultado());
    }

    @Test
    void soloElAutorCorrige() {
        when(catalog.buscarProfesionalDeUsuario("u-doc")).thenReturn(new ProfesionalDeUsuario("prof-2", true));
        when(entradas.buscarPorId("e-1")).thenReturn(original());
        assertEquals("NO_ES_TU_ENTRADA", assertThrows(AccesoProhibidoException.class,
                () -> corregir.ejecutar(new CorregirEntradaUseCase.Comando(DOCTOR, "e-1", TEXTO))).codigo());
        verify(entradas, never()).registrar(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.DENEGADO, log.resultado());
        assertEquals("e-1", log.entradaId());
    }

    @Test
    void corregirUnaEntradaInexistenteEs404() {
        doctorConPerfil();
        assertThrows(NoEncontradoException.class, () -> corregir.ejecutar(new CorregirEntradaUseCase.Comando(DOCTOR, "x", TEXTO)));
    }

    @Test
    void unPacienteNoCorrige() {
        assertThrows(AccesoProhibidoException.class, () -> corregir.ejecutar(new CorregirEntradaUseCase.Comando(PACIENTE, "e-1", TEXTO)));
        verify(entradas, never()).buscarPorId(any());
    }

    // ---- CU-29

    @Test
    void elProfesionalConRelacionLeeLaHistoriaYQuedaEnElLog() {
        doctorConPerfil();
        EntradaClinica[] historia = {original()};
        when(appointment.existeRelacion("prof-1", "pac-1")).thenReturn(true);
        when(entradas.listarDePaciente("pac-1")).thenReturn(historia);

        assertArrayEquals(historia, historiaDePaciente.ejecutar(DOCTOR, "pac-1"));

        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.PERMITIDO, log.resultado());
        assertEquals(AccionAcceso.LECTURA, log.accion());
        assertEquals("pac-1", log.pacienteId());
    }

    @Test
    void sinRelacionNoSeEntregaNadaYSeRegistraElRechazo() {
        doctorConPerfil();
        when(appointment.existeRelacion("prof-1", "pac-9")).thenReturn(false);
        assertEquals("SIN_RELACION_CON_PACIENTE", assertThrows(AccesoProhibidoException.class, () -> historiaDePaciente.ejecutar(DOCTOR, "pac-9")).codigo());
        verify(entradas, never()).listarDePaciente(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.DENEGADO, log.resultado());
        assertEquals("pac-9", log.pacienteId());
    }

    @Test
    void unPacienteOUnAdministrativoNoUsanLaHistoriaDeOtros() {
        assertThrows(AccesoProhibidoException.class, () -> historiaDePaciente.ejecutar(PACIENTE, "pac-2"));
        assertThrows(AccesoProhibidoException.class, () -> historiaDePaciente.ejecutar(new Actor("adm", "ADMINISTRATIVO"), "pac-2"));
        verify(entradas, never()).listarDePaciente(any());
        verify(logs, times(2)).registrar(any());
    }

    @Test
    void appointmentCaidoAlValidarLaRelacionEs503() {
        doctorConPerfil();
        when(appointment.existeRelacion(any(), any())).thenThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido"));
        assertThrows(ServicioNoDisponibleException.class, () -> historiaDePaciente.ejecutar(DOCTOR, "pac-1"));
        assertEquals(ResultadoAcceso.DENEGADO, ultimoLog(1).resultado());
    }

    @Test
    void siNoSePuedeRegistrarLaLecturaNoSeEntregaLaHistoria() {
        doctorConPerfil();
        when(appointment.existeRelacion("prof-1", "pac-1")).thenReturn(true);
        when(entradas.listarDePaciente("pac-1")).thenReturn(new EntradaClinica[] {original()});
        doThrow(new IllegalStateException("bd caida")).when(logs).registrar(any());
        assertThrows(IllegalStateException.class, () -> historiaDePaciente.ejecutar(DOCTOR, "pac-1"));
    }

    // ---- CU-30

    @Test
    void elPacienteSoloLeeLaHistoriaDeSuPropiaCuenta() {
        when(entradas.listarDePaciente("pac-1")).thenReturn(new EntradaClinica[] {original()});
        assertEquals(1, miHistoria.ejecutar(PACIENTE).length);
        verify(entradas).listarDePaciente("pac-1");
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.PERMITIDO, log.resultado());
        assertEquals("pac-1", log.pacienteId());
    }

    @Test
    void quienNoEsPacienteNoUsaMiHistoria() {
        assertEquals("SOLO_PACIENTE", assertThrows(AccesoProhibidoException.class, () -> miHistoria.ejecutar(DOCTOR)).codigo());
        verify(entradas, never()).listarDePaciente(any());
        LogAcceso log = ultimoLog(1);
        assertEquals(ResultadoAcceso.DENEGADO, log.resultado());
        assertNull(log.pacienteId());
    }
}
