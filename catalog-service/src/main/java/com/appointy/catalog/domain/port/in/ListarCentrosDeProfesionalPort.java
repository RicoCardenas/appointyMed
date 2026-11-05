package com.appointy.catalog.domain.port.in;

import com.appointy.catalog.domain.model.Centro;

/** CU-10: centros activos donde atiende el profesional. */
public interface ListarCentrosDeProfesionalPort {

    Centro[] ejecutar(String profesionalId);
}
