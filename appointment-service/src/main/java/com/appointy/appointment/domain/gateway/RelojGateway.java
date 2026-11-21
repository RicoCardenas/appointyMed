package com.appointy.appointment.domain.gateway;

/**
 * Puerto de salida: el reloj de la clinica como valores simples del dominio: instantes en milisegundos, fechas ISO
 * y "minutos locales" (minutos desde 1970 en hora de la clinica).
 */
public interface RelojGateway {

    long ahoraMillis();

    /** Fecha de hoy en la clinica, texto ISO yyyy-MM-dd. */
    String hoy();

    long ahoraMinutosLocales();
}
