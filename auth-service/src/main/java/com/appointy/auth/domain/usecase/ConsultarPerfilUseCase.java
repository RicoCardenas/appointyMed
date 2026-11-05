package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.PacienteConCuenta;

/** CU-04. */
public interface ConsultarPerfilUseCase {

    PacienteConCuenta ejecutar(String usuarioId);
}
