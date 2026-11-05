package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.ListarProfesionalesPorEspecialidadPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ListarProfesionalesPorEspecialidadUseCase implements ListarProfesionalesPorEspecialidadPort {

    private final EspecialidadRepositoryPort especialidades;
    private final ProfesionalRepositoryPort profesionales;

    public ListarProfesionalesPorEspecialidadUseCase(EspecialidadRepositoryPort especialidades, ProfesionalRepositoryPort profesionales) {
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
