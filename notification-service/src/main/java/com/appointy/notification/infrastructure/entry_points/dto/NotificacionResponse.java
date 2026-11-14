package com.appointy.notification.infrastructure.entry_points.dto;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.TipoNotificacion;
import java.time.Instant;

/** El correo del destinatario sale enmascarado (RNF-05); el cuerpo no se expone. */
public record NotificacionResponse(
        String id, String citaId, TipoNotificacion tipo, String destinatario, String asunto, EstadoNotificacion estado, int intentos,
        String ultimoError, Instant programadaPara, Instant enviadaEn, Instant creadoEn) {

    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(n.id(), n.citaId(), n.tipo(), enmascarar(n.destinatarioCorreo()), n.asunto(), n.estado(), n.intentos(),
                n.ultimoError(), Instant.ofEpochMilli(n.programadaPara()), n.enviadaEn() == 0 ? null : Instant.ofEpochMilli(n.enviadaEn()),
                Instant.ofEpochMilli(n.creadoEn()));
    }

    static String enmascarar(String correo) {
        int arroba = correo.indexOf('@');
        if (arroba < 0) return "***";
        return arroba <= 1 ? "***" + correo.substring(arroba) : correo.charAt(0) + "***" + correo.substring(arroba);
    }
}
