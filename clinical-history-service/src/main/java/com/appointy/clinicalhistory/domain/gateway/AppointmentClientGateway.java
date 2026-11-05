package com.appointy.clinicalhistory.domain.gateway;

import com.appointy.clinicalhistory.domain.model.CitaClinica;

/** Llamadas /internal a appointment-service. Cualquier fallo de red, timeout o 5xx lanza ServicioNoDisponibleException (503). */
public interface AppointmentClientGateway {

    /** Devuelve null si la cita no existe. Sin cache: el estado debe ser el actual. */
    CitaClinica buscarCita(String citaId);

    /** CU-27. Las respuestas afirmativas se guardan en cache un tiempo corto; las negativas no. */
    boolean existeRelacion(String profesionalId, String pacienteId);
}
