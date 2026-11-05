package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.HistorialCita;

/** Append-only: no hay actualizar ni borrar (RNF-13). */
public interface HistorialCitaRepositoryGateway {

    void registrar(HistorialCita fila);

    HistorialCita[] listarPorCita(String citaId);
}
