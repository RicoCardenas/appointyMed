package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;

/** Llamadas /internal a catalog-service. Cualquier fallo de red, timeout o 5xx lanza ServicioNoDisponibleException (503). */
public interface CatalogClientGateway {

    ParametrosCita obtenerParametros();

    AsociacionProfesional validarAsociacion(String profesionalId, String especialidadId, String centroId);

    /** Devuelve null si la cuenta no tiene perfil de profesional. */
    ProfesionalDeUsuario buscarProfesionalDeUsuario(String usuarioId);
}
