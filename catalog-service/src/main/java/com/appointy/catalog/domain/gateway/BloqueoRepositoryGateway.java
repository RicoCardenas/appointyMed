package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.Bloqueo;

public interface BloqueoRepositoryGateway {

    Bloqueo guardar(Bloqueo bloqueo);

    int contarPorProfesionalYCentro(String profesionalId, String centroId);
}
