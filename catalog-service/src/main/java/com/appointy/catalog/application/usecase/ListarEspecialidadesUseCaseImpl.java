package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.usecase.ListarEspecialidadesUseCase;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ListarEspecialidadesUseCaseImpl implements ListarEspecialidadesUseCase {

    private final EspecialidadRepositoryGateway especialidades;

    public ListarEspecialidadesUseCaseImpl(EspecialidadRepositoryGateway especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Especialidad[] ejecutar(boolean incluirInactivas) {
        return especialidades.listar(incluirInactivas);
    }
}
