package com.appointy.clinicalhistory.domain.gateway;

import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;

/** Llamadas /internal a catalog-service. Cualquier fallo de red, timeout o 5xx lanza ServicioNoDisponibleException (503). */
public interface CatalogClientGateway {

    /** Devuelve null si la cuenta no tiene perfil de profesional. */
    ProfesionalDeUsuario buscarProfesionalDeUsuario(String usuarioId);
}
