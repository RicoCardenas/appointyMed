package com.appointy.catalog.domain.gateway;

/** Puerto de salida: la hora actual como valores simples del dominio. */
public interface RelojGateway {

    /** Milisegundos desde 1970 en UTC. */
    long ahora();

    /** Fecha de hoy como texto ISO yyyy-MM-dd. */
    String hoy();
}
