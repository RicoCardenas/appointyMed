package com.appointy.notification.infrastructure.driven_adapters.mail;

import com.appointy.notification.domain.gateway.CorreoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Por defecto (sin SMTP configurado): no envia nada y solo deja una linea en el log, sin destinatario ni cuerpo. */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class CorreoSimuladoAdapter implements CorreoGateway {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoSimuladoAdapter.class);

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        LOG.info("Correo simulado (app.mail.enabled=false): asunto='{}'", asunto);
    }
}
