package com.appointy.notification.infrastructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.infrastructure.entry_points.dto.NotificacionResponse;
import com.appointy.notification.infrastructure.entry_points.dto.NotificarCitaRequest;
import com.appointy.notification.infrastructure.entry_points.dto.PaginaResponse;
import org.junit.jupiter.api.Test;

class NotificacionResponseTest {

    static Notificacion n(String correo, long enviadaEn) {
        return new Notificacion("i", "c", TipoNotificacion.CONFIRMACION, correo, "s", "cuerpo", EstadoNotificacion.PENDIENTE, 0, null, 10, enviadaEn, 5);
    }

    @Test
    void enmascaraElCorreoYNoExponeElCuerpo() {
        assertEquals("p***@correo.co", NotificacionResponse.de(n("paciente@correo.co", 0)).destinatario());
        assertEquals("***@b.co", NotificacionResponse.de(n("a@b.co", 0)).destinatario());
        assertEquals("***", NotificacionResponse.de(n("sin-arroba", 0)).destinatario());
    }

    @Test
    void convierteInstantesYPagina() {
        NotificacionResponse r = NotificacionResponse.de(n("paciente@correo.co", 0));
        assertNull(r.enviadaEn());
        assertEquals(10, r.programadaPara().toEpochMilli());
        assertEquals(99, NotificacionResponse.de(n("paciente@correo.co", 99)).enviadaEn().toEpochMilli());
        PaginaResponse p = PaginaResponse.de(new Pagina(new Notificacion[] {n("paciente@correo.co", 0)}, 0, 20, 1, 1));
        assertEquals(1, p.contenido().size());
        assertEquals(1, p.totalPaginas());
    }

    @Test
    void laSolicitudInternaSeConvierteAlModeloDelDominio() {
        var s = new NotificarCitaRequest("CONFIRMADA", "c", "p", null, null, "2026-11-20", "08:00", null).aSolicitud();
        assertEquals("CONFIRMADA", s.tipo());
        assertEquals("08:00", s.horaInicio());
    }
}
