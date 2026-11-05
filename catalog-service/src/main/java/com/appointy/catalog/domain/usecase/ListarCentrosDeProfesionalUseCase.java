package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Centro;

/** CU-10: centros activos donde atiende el profesional. */
public interface ListarCentrosDeProfesionalUseCase {

    Centro[] ejecutar(String profesionalId);
}
