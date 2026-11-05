package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Profesional;

/** Consulta interna de appointment (CU-22, CU-23): el profesional que corresponde a una cuenta de auth. */
public interface BuscarProfesionalPorUsuarioPort {

    /** Devuelve null si la cuenta no tiene perfil de profesional. */
    Profesional ejecutar(String usuarioId);
}
