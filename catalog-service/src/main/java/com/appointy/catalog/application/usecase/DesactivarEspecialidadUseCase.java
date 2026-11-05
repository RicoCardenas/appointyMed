package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.in.DesactivarEspecialidadPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class DesactivarEspecialidadUseCase implements DesactivarEspecialidadPort {

    private final EspecialidadRepositoryPort especialidades;

    public DesactivarEspecialidadUseCase(EspecialidadRepositoryPort especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional
    public void ejecutar(String id) {
        Especialidad actual = especialidades.buscarPorId(id);
        if (actual == null) {
            throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad no existe");
        }
        if (actual.activo()) {
            especialidades.guardar(actual.desactivar());
        }
    }
}
