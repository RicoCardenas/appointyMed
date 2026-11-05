package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.ListarProfesionalesPorEspecialidadUseCase;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ListarProfesionalesPorEspecialidadUseCaseImpl implements ListarProfesionalesPorEspecialidadUseCase {

    private final EspecialidadRepositoryGateway especialidades;
    private final ProfesionalRepositoryGateway profesionales;

    public ListarProfesionalesPorEspecialidadUseCaseImpl(EspecialidadRepositoryGateway especialidades, ProfesionalRepositoryGateway profesionales) {
        this.especialidades = especialidades;
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public Profesional[] ejecutar(String especialidadId) {
        Especialidad especialidad = especialidades.buscarPorId(especialidadId);
        if (especialidad == null || !especialidad.activo()) {
            throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad no existe o no esta disponible");
        }
        return profesionales.listarActivosPorEspecialidad(especialidadId);
    }
}
