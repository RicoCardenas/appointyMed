package com.appointy.notification.infrastructure.driven_adapters.rest_client;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.infrastructure.config.AuthProperties;
import com.appointy.notification.infrastructure.config.CorrelationIdFilter;
import com.appointy.notification.infrastructure.config.InternalProperties;
import com.appointy.notification.infrastructure.security.ApiKeyFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** GET /internal/usuarios/{id}/contacto en auth-service. 404 = null; cualquier otro fallo = 503. */
@Component
public class UsuarioRestClientAdapter implements UsuarioClientGateway {

    private final RestClient cliente;
    private final String llave;

    public UsuarioRestClientAdapter(AuthProperties propiedades, InternalProperties interna) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = interna.apiKey();
    }

    @Override
    public String correoDe(String usuarioId) {
        try {
            Contacto contacto = cliente.get().uri("/internal/usuarios/{id}/contacto", usuarioId)
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(Contacto.class);
            return contacto == null ? null : contacto.correo();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw new ServicioNoDisponibleException("AUTH_NO_DISPONIBLE", "El servicio de autenticacion no respondio");
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("AUTH_NO_DISPONIBLE", "El servicio de autenticacion no respondio");
        }
    }

    record Contacto(String correo) {}
}
