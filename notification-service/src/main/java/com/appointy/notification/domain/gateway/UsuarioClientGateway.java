package com.appointy.notification.domain.gateway;

/** Cliente de /internal de auth-service. */
public interface UsuarioClientGateway {

    /** Devuelve el correo del usuario, o null si no existe. Lanza ServicioNoDisponibleException si auth no responde. */
    String correoDe(String usuarioId);
}
