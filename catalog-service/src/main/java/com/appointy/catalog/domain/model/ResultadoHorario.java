package com.appointy.catalog.domain.model;

/** El horario siempre queda guardado; {@code franjasGeneradas} dice si appointment pudo crear las franjas. */
public record ResultadoHorario(HorarioAtencion horario, boolean franjasGeneradas, int franjasCreadas, int franjasExistentes, String mensaje) {}
