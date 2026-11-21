package com.appointy.notification.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.notification.domain.exception.NoEncontradoException;
import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.gateway.CorreoGateway;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.domain.gateway.RelojGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacionUseCasesTest {

    static final String CITA = "22222222-2222-2222-2222-222222222222";
    static final String PACIENTE = "33333333-3333-3333-3333-333333333333";
    static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    // 2026-11-14 12:00 UTC
    static final Instant AHORA = Instant.parse("2026-11-14T12:00:00Z");

    final Map<String, Notificacion> bd = new LinkedHashMap<>();
    final List<String> enviados = new ArrayList<>();
    boolean correoFalla;
    boolean authCaido;
    String correoPaciente = "paciente@correo.co";
    RelojGateway reloj = Dobles.reloj(Clock.fixed(AHORA, ZoneOffset.UTC), BOGOTA);

    final NotificacionRepositoryGateway repo = new NotificacionRepositoryGateway() {
        public Notificacion guardar(Notificacion n) {
            bd.put(n.id(), n);
            return n;
        }

        public Notificacion buscarPorId(String id) {
            return bd.get(id);
        }

        public Notificacion buscarPorClave(String citaId, TipoNotificacion tipo, long programadaPara) {
            return bd.values().stream().filter(n -> n.citaId().equals(citaId) && n.tipo() == tipo && n.programadaPara() == programadaPara)
                    .findFirst().orElse(null);
        }

        public Notificacion[] recordatoriosVencidos(long ahora, int limite) {
            return bd.values().stream().filter(n -> n.tipo() == TipoNotificacion.RECORDATORIO && n.estado() == EstadoNotificacion.PENDIENTE
                    && n.programadaPara() <= ahora).limit(limite).toArray(Notificacion[]::new);
        }

        public Notificacion[] fallidasReintentables(int max, int limite) {
            return bd.values().stream().filter(n -> n.estado() == EstadoNotificacion.FALLIDA && n.intentos() < max).limit(limite)
                    .toArray(Notificacion[]::new);
        }

        public int cancelarRecordatoriosPendientes(String citaId) {
            int[] c = {0};
            new ArrayList<>(bd.values()).forEach(n -> {
                if (n.citaId().equals(citaId) && n.tipo() == TipoNotificacion.RECORDATORIO && n.estado() == EstadoNotificacion.PENDIENTE) {
                    bd.put(n.id(), n.comoCancelada());
                    c[0]++;
                }
            });
            return c[0];
        }

        public Pagina listar(FiltroNotificaciones f, int pagina, int tamano) {
            Notificacion[] todas = bd.values().toArray(Notificacion[]::new);
            return new Pagina(todas, pagina, tamano, todas.length, 1);
        }

        public Notificacion[] listarPorCita(String citaId) {
            return bd.values().stream().filter(n -> n.citaId().equals(citaId)).toArray(Notificacion[]::new);
        }

        public Estadisticas estadisticas() {
            return new Estadisticas(1, 2, 3, 4, 5, 6, 7, 8);
        }
    };

    final CorreoGateway correo = (destino, asunto, cuerpo) -> {
        if (correoFalla) throw new ServicioNoDisponibleException("CORREO_NO_DISPONIBLE", "x");
        enviados.add(destino + "|" + asunto);
    };

    final UsuarioClientGateway usuarios = id -> {
        if (authCaido) throw new ServicioNoDisponibleException("AUTH_NO_DISPONIBLE", "x");
        return correoPaciente;
    };

    Despachador despachador;
    NotificacionUseCase notificar;

    @BeforeEach
    void preparar() {
        despachador = new Despachador(correo, repo, reloj);
        notificar = new NotificacionUseCase(repo, usuarios, despachador, reloj, 0, 0, Dobles.IDS);
    }

    static SolicitudCita solicitud(String tipo, String fecha, String hora) {
        return new SolicitudCita(tipo, CITA, PACIENTE, null, null, fecha, hora, null);
    }

    @Test
    void confirmarEnviaElCorreoYProgramaElRecordatorio() {
        Notificacion n = notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        assertEquals(EstadoNotificacion.ENVIADA, n.estado());
        assertEquals(1, enviados.size());
        // 08:00 Bogota = 13:00 UTC; el recordatorio queda 24 h antes
        assertEquals(Instant.parse("2026-11-20T13:00:00Z").toEpochMilli(), n.programadaPara());
        Notificacion[] todas = repo.listarPorCita(CITA);
        assertEquals(2, todas.length);
        assertEquals(TipoNotificacion.RECORDATORIO, todas[1].tipo());
        assertEquals(EstadoNotificacion.PENDIENTE, todas[1].estado());
        assertEquals(Instant.parse("2026-11-19T13:00:00Z").toEpochMilli(), todas[1].programadaPara());
    }

    @Test
    void sinRecordatorioSiFaltaMenosDe24Horas() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-15", "05:00"));
        assertEquals(1, repo.listarPorCita(CITA).length);
    }

    @Test
    void repetirLaMismaSolicitudNoDuplicaElCorreo() {
        Notificacion a = notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        Notificacion b = notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        assertEquals(a.id(), b.id());
        assertEquals(1, enviados.size());
        assertEquals(2, repo.listarPorCita(CITA).length);
    }

    @Test
    void reprogramarCancelaElRecordatorioViejoYCreaUnoNuevo() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        notificar.notificarCita(solicitud("REPROGRAMADA", "2026-11-25", "09:00"));
        long canceladas = bd.values().stream().filter(n -> n.estado() == EstadoNotificacion.CANCELADA).count();
        long pendientes = bd.values().stream().filter(n -> n.tipo() == TipoNotificacion.RECORDATORIO && n.estado() == EstadoNotificacion.PENDIENTE).count();
        assertEquals(1, canceladas);
        assertEquals(1, pendientes);
    }

    @Test
    void cancelarCancelaElRecordatorioYNoCreaOtro() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        notificar.notificarCita(solicitud("CANCELADA", "2026-11-20", "08:00"));
        assertEquals(0, bd.values().stream().filter(n -> n.estado() == EstadoNotificacion.PENDIENTE).count());
        assertEquals(3, repo.listarPorCita(CITA).length);
    }

    @Test
    void siElCorreoFallaQuedaFallidaSinPropagarElError() {
        correoFalla = true;
        Notificacion n = notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        assertEquals(EstadoNotificacion.FALLIDA, n.estado());
        assertEquals(1, n.intentos());
        assertEquals("CORREO_NO_DISPONIBLE", n.ultimoError());
    }

    @Test
    void unErrorInesperadoSeGuardaConElNombreDeLaClase() {
        CorreoGateway roto = (d, a, c) -> {
            throw new IllegalStateException("contiene datos");
        };
        Notificacion n = new Despachador(roto, repo, reloj).despachar(
                Notificacion.nueva("a1111111-1111-1111-1111-111111111111", CITA, TipoNotificacion.CONFIRMACION, "a@b.co", "s", "c", 1, 1));
        assertEquals("IllegalStateException", n.ultimoError());
    }

    @Test
    void sinDestinatarioOConAuthCaidoNoSeRegistraNada() {
        correoPaciente = null;
        assertThrows(NoEncontradoException.class, () -> notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00")));
        correoPaciente = "a@b.co";
        authCaido = true;
        assertThrows(ServicioNoDisponibleException.class, () -> notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00")));
        assertTrue(bd.isEmpty());
    }

    @Test
    void unaFechaConFormatoCorrectoPeroInexistenteEsValidacionNoError500() {
        assertEquals("FECHA_INVALIDA", assertThrows(ValidacionException.class, () -> notificar.notificarCita(solicitud("CONFIRMADA", "2026-13-45", "08:00"))).codigo());
        assertEquals("FECHA_INVALIDA", assertThrows(ValidacionException.class, () -> notificar.notificarCita(solicitud("CONFIRMADA", "2026-02-30", "08:00"))).codigo());
        assertTrue(bd.isEmpty());
    }

    @Test
    void rechazaSolicitudesInvalidas() {
        assertThrows(ValidacionException.class, () -> notificar.notificarCita(solicitud("RARA", "2026-11-20", "08:00")));
    }

    @Test
    void elJobEnviaSoloLosRecordatoriosVencidos() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        NotificacionUseCase job = new NotificacionUseCase(repo, null, despachador, reloj, 10, 0, Dobles.IDS);
        assertEquals(0, job.enviarRecordatorios().procesadas());
        // 6 dias despues el recordatorio ya vencio
        RelojGateway futuro = Dobles.reloj(Clock.fixed(AHORA.plusSeconds(6 * 86400L), ZoneOffset.UTC), BOGOTA);
        ResultadoLote r = new NotificacionUseCase(repo, null, new Despachador(correo, repo, futuro), futuro, 10, 0, Dobles.IDS).enviarRecordatorios();
        assertEquals(new ResultadoLote(1, 1, 0), r);
        assertEquals(2, enviados.size());
    }

    @Test
    void elJobDeRecordatoriosContinuaSiUnoFalla() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        correoFalla = true;
        RelojGateway futuro = Dobles.reloj(Clock.fixed(AHORA.plusSeconds(6 * 86400L), ZoneOffset.UTC), BOGOTA);
        ResultadoLote r = new NotificacionUseCase(repo, null, new Despachador(correo, repo, futuro), futuro, 10, 0, Dobles.IDS).enviarRecordatorios();
        assertEquals(new ResultadoLote(1, 0, 1), r);
    }

    @Test
    void reintentaLasFallidasHastaElMaximo() {
        correoFalla = true;
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-15", "08:00"));
        NotificacionUseCase job = new NotificacionUseCase(repo, null, despachador, null, 10, 3, Dobles.IDS);
        assertEquals(new ResultadoLote(1, 0, 1), job.reintentarFallidas()); // intento 2
        assertEquals(new ResultadoLote(1, 0, 1), job.reintentarFallidas()); // intento 3
        assertEquals(0, job.reintentarFallidas().procesadas()); // ya llego al maximo
        correoFalla = false;
        Notificacion n = bd.values().iterator().next();
        assertEquals(3, n.intentos());
        assertEquals(EstadoNotificacion.FALLIDA, n.estado());
    }

    @Test
    void reintentoExitosoCambiaAEnviada() {
        correoFalla = true;
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-15", "08:00"));
        correoFalla = false;
        assertEquals(new ResultadoLote(1, 1, 0), new NotificacionUseCase(repo, null, despachador, null, 10, 3, Dobles.IDS).reintentarFallidas());
    }

    @Test
    void reenviaUnaFallidaYRechazaLoDemas() {
        correoFalla = true;
        Notificacion fallida = notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-15", "08:00"));
        correoFalla = false;
        NotificacionUseCase reenviar = new NotificacionUseCase(repo, null, despachador, null, 0, 0, Dobles.IDS);
        assertEquals(EstadoNotificacion.ENVIADA, reenviar.reenviarNotificacion(fallida.id()).estado());
        assertThrows(com.appointy.notification.domain.exception.ConflictoException.class, () -> reenviar.reenviarNotificacion(fallida.id()));
        assertThrows(NoEncontradoException.class, () -> reenviar.reenviarNotificacion("00000000-0000-0000-0000-000000000000"));
    }

    @Test
    void listadoYConsultas() {
        notificar.notificarCita(solicitud("CONFIRMADA", "2026-11-20", "08:00"));
        ConsultaNotificacionUseCase listar = new ConsultaNotificacionUseCase(repo);
        assertEquals(2, listar.listarNotificaciones(new FiltroNotificaciones(null, null, null), 0, 20).totalElementos());
        assertThrows(ValidacionException.class, () -> listar.listarNotificaciones(null, -1, 20));
        ConsultaNotificacionUseCase deCita = new ConsultaNotificacionUseCase(repo);
        assertEquals(2, deCita.listarNotificacionesDeCita(CITA).length);
        assertThrows(ValidacionException.class, () -> deCita.listarNotificacionesDeCita(" "));
        assertThrows(ValidacionException.class, () -> deCita.listarNotificacionesDeCita(null));
        assertEquals(36L, new ConsultaNotificacionUseCase(repo).consultarEstadisticas().pendientes() + 35);
        assertNull(repo.buscarPorId("x"));
        assertNotNull(repo.buscarPorId(bd.keySet().iterator().next()));
    }
}
