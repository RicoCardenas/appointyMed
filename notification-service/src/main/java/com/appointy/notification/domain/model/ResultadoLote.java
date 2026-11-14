package com.appointy.notification.domain.model;

/** Resultado de un job (CU-34, CU-35): cuantas notificaciones se intentaron y como terminaron. */
public record ResultadoLote(int procesadas, int enviadas, int fallidas) {}
