package com.appointy.notification.infrastructure.driven_adapters.mail;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.domain.gateway.CorreoGateway;
import com.appointy.notification.infrastructure.config.BrevoProperties;
import com.appointy.notification.infrastructure.driven_adapters.rest_client.ClienteHttp;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Envio real por la API HTTP de Brevo (app.mail.enabled=true y app.mail.provider=brevo). El remitente debe estar
 * verificado en Brevo. Nunca registra el destinatario ni el cuerpo (RNF-05); el fallo siempre es 503.
 */
@Component
@ConditionalOnExpression("'${app.mail.enabled:false}' == 'true' and '${app.mail.provider:smtp}' == 'brevo'")
public class CorreoBrevoApiAdapter implements CorreoGateway {

    private final RestClient cliente;
    private final String llave;
    private final String remitente;

    public CorreoBrevoApiAdapter(BrevoProperties propiedades, Environment entorno) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = propiedades.apiKey();
        this.remitente = entorno.getProperty("app.mail.from", "no-responder@appointy.local");
    }

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        Mensaje mensaje = new Mensaje(new Persona(remitente), List.of(new Persona(destino)), asunto, cuerpo);
        try {
            cliente.post().uri("/v3/smtp/email").header("api-key", llave).body(mensaje).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("CORREO_NO_DISPONIBLE", "El servicio de correo no acepto el mensaje");
        }
    }

    record Persona(String email) {}

    record Mensaje(Persona sender, List<Persona> to, String subject, String textContent) {}
}
