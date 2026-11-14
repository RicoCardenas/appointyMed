package com.appointy.appointment.domain.model;

/** Cualquier campo en null significa "sin filtrar por ese campo". */
public record FiltroCitas(EstadoCita estado, String profesionalId, String pacienteId) {}
