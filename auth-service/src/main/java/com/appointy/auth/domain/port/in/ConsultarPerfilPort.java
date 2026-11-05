package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-04. */
public interface ConsultarPerfilPort {

    PacienteConCuenta ejecutar(String usuarioId);
}
