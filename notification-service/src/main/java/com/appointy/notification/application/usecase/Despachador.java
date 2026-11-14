package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.exception.DomainException;
import com.appointy.notification.domain.gateway.CorreoGateway;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Notificacion;
import java.time.Clock;

/**
 * Intenta enviar una notificacion y guarda el resultado. El envio no ocurre dentro de una transaccion de base de datos
 * (el servidor de correo puede tardar). Un fallo nunca se propaga: queda FALLIDA con un codigo, sin el mensaje de la causa.
 */
public class Despachador {

    private final CorreoGateway correo;
    private final NotificacionRepositoryGateway notificaciones;
    private final Clock reloj;

    public Despachador(CorreoGateway correo, NotificacionRepositoryGateway notificaciones, Clock reloj) {
        this.correo = correo;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    public Notificacion despachar(Notificacion notificacion) {
        Notificacion resultado;
        try {
            correo.enviar(notificacion.destinatarioCorreo(), notificacion.asunto(), notificacion.cuerpo());
            resultado = notificacion.comoEnviada(reloj.millis());
        } catch (RuntimeException e) {
            resultado = notificacion.comoFallida(codigoDe(e));
        }
        return notificaciones.guardar(resultado);
    }

    private static String codigoDe(RuntimeException e) {
        return e instanceof DomainException d ? d.codigo() : e.getClass().getSimpleName();
    }
}
