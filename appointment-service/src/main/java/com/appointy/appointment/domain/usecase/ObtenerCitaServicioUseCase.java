package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.Cita;

/** Consulta de una cita por otro servicio (clinical, CU-28): sin reglas de propiedad, solo ids y estado. Lanza 404 si no existe. */
public interface ObtenerCitaServicioUseCase {

    Cita ejecutar(String citaId);
}
