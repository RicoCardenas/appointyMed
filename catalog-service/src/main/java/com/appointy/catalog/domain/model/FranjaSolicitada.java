package com.appointy.catalog.domain.model;

/** Franja que catalog pide crear a appointment: fecha ISO y horas en minutos desde medianoche. */
public record FranjaSolicitada(String fecha, int horaInicio, int horaFin) {}
