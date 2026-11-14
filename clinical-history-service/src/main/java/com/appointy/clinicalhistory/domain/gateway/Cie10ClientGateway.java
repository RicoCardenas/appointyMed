package com.appointy.clinicalhistory.domain.gateway;

import com.appointy.clinicalhistory.domain.model.Diagnostico;

/** Puerto hacia la API externa del catalogo CIE-10 (RA-06). Solo recibe el termino buscado, nunca datos del paciente. */
public interface Cie10ClientGateway {

    /** Hasta {@code maximo} diagnosticos que coinciden. Lanza ServicioNoDisponibleException si la API externa no responde. */
    Diagnostico[] buscar(String termino, int maximo);
}
