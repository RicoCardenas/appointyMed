package com.appointy.appointment.domain.port.out;

import com.appointy.appointment.domain.model.HistorialCita;

/** Append-only: no hay actualizar ni borrar (RNF-13). */
public interface HistorialCitaRepositoryPort {

    void registrar(HistorialCita fila);

    HistorialCita[] listarPorCita(String citaId);
}
