package com.appointy.clinicalhistory.domain.model;

/** Texto de una atencion o de su correccion. Es dato clinico: nunca va a logs ni a mensajes de error. */
public record ContenidoClinico(String motivo, String diagnostico, String notas, String indicaciones) {}
