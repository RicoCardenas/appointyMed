package com.appointy.appointment.infrastructure.driven_adapters.rest_client;

import com.appointy.appointment.domain.exception.ServicioNoDisponibleException;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.port.out.NotificacionClientPort;
import com.appointy.appointment.infrastructure.config.CorrelationIdFilter;
import com.appointy.appointment.infrastructure.config.InternalProperties;
import com.appointy.appointment.infrastructure.config.NotificationProperties;
import com.appointy.appointment.infrastructure.security.ApiKeyFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** POST /internal/notificaciones en notification-service (Fase 5). El oyente posterior al commit atrapa cualquier fallo. */
@Component
public class NotificationRestClientAdapter implements NotificacionClientPort {

    private final RestClient cliente;
    private final String llave;

    public NotificationRestClientAdapter(NotificationProperties propiedades, InternalProperties interna) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = interna.apiKey();
    }

    @Override
    public void notificar(EventoCita evento) {
        try {
            cliente.post().uri("/internal/notificaciones")
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .body(new Solicitud(evento.tipo(), evento.citaId(), evento.pacienteId(), evento.profesionalId(), evento.centroId(),
                            evento.fecha(), String.format("%02d:%02d", evento.horaInicio() / 60, evento.horaInicio() % 60), evento.actorId()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("NOTIFICATION_NO_DISPONIBLE", "El servicio de notificaciones no respondio");
        }
    }

    record Solicitud(String tipo, String citaId, String pacienteId, String profesionalId, String centroId, String fecha, String horaInicio,
            String actorId) {}
}
