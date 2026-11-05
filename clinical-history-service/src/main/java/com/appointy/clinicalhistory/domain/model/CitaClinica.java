package com.appointy.clinicalhistory.domain.model;

/** Lo que clinical necesita saber de una cita de appointment: solo ids y estado, sin datos personales. */
public record CitaClinica(String id, String pacienteId, String profesionalId, EstadoCita estado) {}
