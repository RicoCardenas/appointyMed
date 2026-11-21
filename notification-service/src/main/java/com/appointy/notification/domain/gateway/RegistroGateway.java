package com.appointy.notification.domain.gateway;

/** Puerto de salida: registro de eventos tecnicos. Nunca recibe datos sensibles ni clinicos. */
public interface RegistroGateway {

    void info(String mensaje);

    void error(String mensaje);
}
