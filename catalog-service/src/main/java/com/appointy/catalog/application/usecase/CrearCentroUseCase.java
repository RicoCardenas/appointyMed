package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.port.in.CrearCentroPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearCentroUseCase implements CrearCentroPort {

    private final CentroRepositoryPort centros;

    public CrearCentroUseCase(CentroRepositoryPort centros) {
        this.centros = centros;
    }

    @Override
    @Transactional
    public Centro ejecutar(Comando c) {
        return centros.guardar(new Centro(UUID.randomUUID().toString(), c.nombre().trim(), c.direccion().trim(), c.ciudad().trim(), c.telefono(), true));
    }
}
