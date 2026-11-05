package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.Bloqueo;

public interface BloqueoRepositoryPort {

    Bloqueo guardar(Bloqueo bloqueo);

    int contarPorProfesionalYCentro(String profesionalId, String centroId);
}
