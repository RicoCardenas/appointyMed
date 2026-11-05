package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.PacienteConCuenta;
import java.util.UUID;

/** CU-04. */
public interface ConsultarPerfilPort {

    PacienteConCuenta ejecutar(UUID usuarioId);
}
