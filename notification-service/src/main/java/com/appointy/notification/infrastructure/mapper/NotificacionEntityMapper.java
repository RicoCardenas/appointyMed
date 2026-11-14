package com.appointy.notification.infrastructure.mapper;

import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.entity.NotificacionEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Traduce los tipos simples del dominio (String, long) a los de la entidad (UUID, Instant). */
@Component
public class NotificacionEntityMapper {

    public Notificacion aDominio(NotificacionEntity e) {
        return new Notificacion(e.getId().toString(), e.getCitaId().toString(), e.getTipo(), e.getDestinatarioCorreo(), e.getAsunto(),
                e.getCuerpo(), e.getEstado(), e.getIntentos(), e.getUltimoError(), e.getProgramadaPara().toEpochMilli(),
                e.getEnviadaEn() == null ? 0 : e.getEnviadaEn().toEpochMilli(), e.getCreadoEn().toEpochMilli());
    }

    public NotificacionEntity aEntidad(Notificacion n) {
        NotificacionEntity e = new NotificacionEntity();
        e.setId(UUID.fromString(n.id()));
        e.setCitaId(UUID.fromString(n.citaId()));
        e.setTipo(n.tipo());
        e.setDestinatarioCorreo(n.destinatarioCorreo());
        e.setAsunto(n.asunto());
        e.setCuerpo(n.cuerpo());
        e.setEstado(n.estado());
        e.setIntentos(n.intentos());
        e.setUltimoError(n.ultimoError());
        e.setProgramadaPara(Instant.ofEpochMilli(n.programadaPara()));
        e.setEnviadaEn(n.enviadaEn() == 0 ? null : Instant.ofEpochMilli(n.enviadaEn()));
        e.setCreadoEn(Instant.ofEpochMilli(n.creadoEn()));
        return e;
    }
}
