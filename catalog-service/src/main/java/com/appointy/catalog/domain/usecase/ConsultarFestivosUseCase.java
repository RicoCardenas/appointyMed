package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Festivo;

/** Festivos del anio segun la API externa (RA-06). Lanza ServicioNoDisponibleException si no responde. */
public interface ConsultarFestivosUseCase {

    Festivo[] ejecutar(int anio);
}
