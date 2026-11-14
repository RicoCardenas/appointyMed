package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.Festivo;

/** Puerto hacia la API externa de dias festivos (RA-06). */
public interface FestivosClientGateway {

    /** Festivos del anio, por fecha. Lanza ServicioNoDisponibleException si la API externa no responde. */
    Festivo[] festivos(int anio);
}
