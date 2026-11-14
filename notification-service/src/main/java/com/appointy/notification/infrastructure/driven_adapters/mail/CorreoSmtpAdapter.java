package com.appointy.notification.infrastructure.driven_adapters.mail;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.domain.gateway.CorreoGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envio real por SMTP (app.mail.enabled=true). Nunca registra el destinatario ni el cuerpo (RNF-05). */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class CorreoSmtpAdapter implements CorreoGateway {

    private final JavaMailSender enviador;
    private final String remitente;

    public CorreoSmtpAdapter(JavaMailSender enviador, org.springframework.core.env.Environment entorno) {
        this.enviador = enviador;
        this.remitente = entorno.getProperty("app.mail.from", "no-responder@appointy.local");
    }

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            enviador.send(mensaje);
        } catch (MailException e) {
            throw new ServicioNoDisponibleException("CORREO_NO_DISPONIBLE", "El servidor de correo no acepto el mensaje");
        }
    }
}
