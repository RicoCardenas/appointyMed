package com.appointy.appointment.domain.model;

/** Cita junto con la franja que ocupa, para mostrarla con su fecha y hora. */
public record CitaConFranja(Cita cita, Franja franja) {}
