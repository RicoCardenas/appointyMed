package com.appointy.notification.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.infrastructure.driven_adapters.mail.CorreoSimuladoAdapter;
import com.appointy.notification.infrastructure.driven_adapters.mail.CorreoSmtpAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.env.MockEnvironment;

class CorreoAdaptersTest {

    @Test
    void smtpArmaElMensajeConElRemitenteConfigurado() {
        JavaMailSender enviador = mock(JavaMailSender.class);
        new CorreoSmtpAdapter(enviador, new MockEnvironment().withProperty("app.mail.from", "clinica@appointy.test"))
                .enviar("paciente@correo.co", "Asunto", "Cuerpo");
        ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(enviador).send(mensaje.capture());
        assertEquals("clinica@appointy.test", mensaje.getValue().getFrom());
        assertEquals("paciente@correo.co", mensaje.getValue().getTo()[0]);
        assertEquals("Asunto", mensaje.getValue().getSubject());
    }

    @Test
    void smtpUsaUnRemitentePorDefectoYTraduceLosFallos() {
        JavaMailSender enviador = mock(JavaMailSender.class);
        doThrow(new MailSendException("smtp caido")).when(enviador).send(any(SimpleMailMessage.class));
        CorreoSmtpAdapter adaptador = new CorreoSmtpAdapter(enviador, new MockEnvironment());
        ServicioNoDisponibleException e = assertThrows(ServicioNoDisponibleException.class, () -> adaptador.enviar("a@b.co", "s", "c"));
        assertEquals("CORREO_NO_DISPONIBLE", e.codigo());
    }

    @Test
    void elSimuladoNoFalla() {
        new CorreoSimuladoAdapter().enviar("a@b.co", "s", "c");
    }
}
