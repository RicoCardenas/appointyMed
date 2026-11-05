package com.appointy.appointment.domain.port.out;

import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;

/** Llamadas /internal a catalog-service. Cualquier fallo de red, timeout o 5xx lanza ServicioNoDisponibleException (503). */
public interface CatalogClientPort {

    ParametrosCita obtenerParametros();

    AsociacionProfesional validarAsociacion(String profesionalId, String especialidadId, String centroId);

    /** Devuelve null si la cuenta no tiene perfil de profesional. */
    ProfesionalDeUsuario buscarProfesionalDeUsuario(String usuarioId);
}
