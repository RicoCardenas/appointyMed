package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.in.ListarEspecialidadesPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ListarEspecialidadesUseCase implements ListarEspecialidadesPort {

    private final EspecialidadRepositoryPort especialidades;

    public ListarEspecialidadesUseCase(EspecialidadRepositoryPort especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Especialidad[] ejecutar(boolean incluirInactivas) {
        return especialidades.listar(incluirInactivas);
    }
}
