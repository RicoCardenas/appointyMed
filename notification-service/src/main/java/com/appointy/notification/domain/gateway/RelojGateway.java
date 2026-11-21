package com.appointy.notification.domain.gateway;

/** Puerto de salida: el reloj y la zona horaria de la clinica como valores simples del dominio. */
public interface RelojGateway {

    /** Milisegundos desde 1970 en UTC. */
    long ahora();

    /**
     * Convierte la fecha ISO y la hora HH:mm locales de la clinica en milisegundos UTC.
     * Lanza ValidacionException("FECHA_INVALIDA") si la fecha u hora no existe (2026-02-30).
     */
    long inicioEnMillis(String fecha, String hora);
}
