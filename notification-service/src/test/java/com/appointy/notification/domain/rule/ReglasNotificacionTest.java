package com.appointy.notification.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.notification.domain.exception.ConflictoException;
import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;
import org.junit.jupiter.api.Test;

class ReglasNotificacionTest {

    static final String UUID1 = "11111111-1111-1111-1111-111111111111";

    static SolicitudCita solicitud(String tipo, String citaId, String fecha, String hora) {
        return new SolicitudCita(tipo, citaId, UUID1, null, null, fecha, hora, null);
    }

    @Test
    void mapeaLosTiposDeCita() {
        assertEquals(TipoNotificacion.CONFIRMACION, ReglasNotificacion.tipoDe("CONFIRMADA"));
        assertEquals(TipoNotificacion.REPROGRAMACION, ReglasNotificacion.tipoDe("REPROGRAMADA"));
        assertEquals(TipoNotificacion.CANCELACION, ReglasNotificacion.tipoDe("CANCELADA"));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.tipoDe("OTRA"));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.tipoDe(null));
    }

    @Test
    void validaLaSolicitud() {
        ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", UUID1, "2026-11-20", "08:30"));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(null));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("X", UUID1, "2026-11-20", "08:30")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", "no-uuid", "2026-11-20", "08:30")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", null, "2026-11-20", "08:30")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", UUID1, "20/11/2026", "08:30")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", UUID1, null, "08:30")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", UUID1, "2026-11-20", "24:00")));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(solicitud("CONFIRMADA", UUID1, "2026-11-20", null)));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarSolicitud(
                new SolicitudCita("CONFIRMADA", UUID1, "x", null, null, "2026-11-20", "08:30", null)));
    }

    @Test
    void construyeAsuntoYCuerpoPorTipo() {
        for (TipoNotificacion t : TipoNotificacion.values()) {
            assertFalse(ReglasNotificacion.asunto(t).isBlank());
            String cuerpo = ReglasNotificacion.cuerpo(t, "2026-11-20", "08:30", UUID1);
            assertTrue(cuerpo.contains("2026-11-20") && cuerpo.contains("08:30") && cuerpo.contains(UUID1));
        }
        assertEquals("Cita cancelada", ReglasNotificacion.asunto(TipoNotificacion.CANCELACION));
    }

    @Test
    void elRecordatorioSoloSeProgramaConMasDe24Horas() {
        long hora = 3_600_000L;
        long ahora = 1_000_000_000L;
        assertTrue(ReglasNotificacion.requiereRecordatorio(TipoNotificacion.CONFIRMACION, ahora + 25 * hora, ahora));
        assertTrue(ReglasNotificacion.requiereRecordatorio(TipoNotificacion.REPROGRAMACION, ahora + 25 * hora, ahora));
        assertFalse(ReglasNotificacion.requiereRecordatorio(TipoNotificacion.CONFIRMACION, ahora + 24 * hora, ahora));
        assertFalse(ReglasNotificacion.requiereRecordatorio(TipoNotificacion.CANCELACION, ahora + 48 * hora, ahora));
        assertEquals(ahora + hora, ReglasNotificacion.momentoRecordatorio(ahora + 25 * hora));
    }

    static Notificacion con(EstadoNotificacion estado) {
        return new Notificacion(UUID1, UUID1, TipoNotificacion.CONFIRMACION, "a@b.co", "s", "c", estado, 0, null, 1, 0, 1);
    }

    @Test
    void soloSeReenvianPendientesYFallidas() {
        ReglasNotificacion.validarReenvio(con(EstadoNotificacion.PENDIENTE));
        ReglasNotificacion.validarReenvio(con(EstadoNotificacion.FALLIDA));
        assertEquals("YA_ENVIADA", assertThrows(ConflictoException.class, () -> ReglasNotificacion.validarReenvio(con(EstadoNotificacion.ENVIADA))).codigo());
        assertEquals("NOTIFICACION_CANCELADA",
                assertThrows(ConflictoException.class, () -> ReglasNotificacion.validarReenvio(con(EstadoNotificacion.CANCELADA))).codigo());
    }

    @Test
    void validaLaPaginacion() {
        ReglasNotificacion.validarPaginacion(0, 1);
        ReglasNotificacion.validarPaginacion(3, 100);
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarPaginacion(-1, 20));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarPaginacion(0, 0));
        assertThrows(ValidacionException.class, () -> ReglasNotificacion.validarPaginacion(0, 101));
    }

    @Test
    void lasTransicionesDelRegistroSonCorrectas() {
        Notificacion n = con(EstadoNotificacion.PENDIENTE);
        Notificacion enviada = n.comoEnviada(99);
        assertEquals(EstadoNotificacion.ENVIADA, enviada.estado());
        assertEquals(1, enviada.intentos());
        assertEquals(99, enviada.enviadaEn());
        Notificacion fallida = n.comoFallida("X");
        assertEquals(EstadoNotificacion.FALLIDA, fallida.estado());
        assertEquals("X", fallida.ultimoError());
        assertEquals(EstadoNotificacion.CANCELADA, n.comoCancelada().estado());
        assertEquals(EstadoNotificacion.PENDIENTE, Notificacion.nueva("i", "c", TipoNotificacion.RECORDATORIO, "a@b.co", "s", "c", 5, 6).estado());
    }
}
