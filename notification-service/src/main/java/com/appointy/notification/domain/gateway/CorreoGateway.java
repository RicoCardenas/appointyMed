package com.appointy.notification.domain.gateway;

/** Puerto de salida hacia el servidor de correo. */
public interface CorreoGateway {

    /** Lanza ServicioNoDisponibleException si el correo no pudo entregarse al servidor. */
    void enviar(String destino, String asunto, String cuerpo);
}
