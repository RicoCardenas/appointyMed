package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.ResultadoGeneracion;

/**
 * Cliente hacia appointment-service (/internal). Ambas operaciones son idempotentes y lanzan
 * {@code ServicioNoDisponibleException} si appointment no responde a tiempo o falla.
 */
public interface AppointmentClientPort {

    /** CU-25: crea las franjas que aun no existan. */
    ResultadoGeneracion generarFranjas(String profesionalId, String centroId, FranjaSolicitada[] franjas);

    /** CU-26: bloquea las franjas del intervalo; lanza {@code ConflictoCitasException} si hay citas CONFIRMADAS. */
    void bloquearFranjas(String profesionalId, String centroId, String fechaInicio, String fechaFin);
}
