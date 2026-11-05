package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.usecase.ListarHorariosUseCase;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ListarHorariosUseCaseImpl implements ListarHorariosUseCase {

    private final ProfesionalRepositoryGateway profesionales;
    private final HorarioRepositoryGateway horarios;

    public ListarHorariosUseCaseImpl(ProfesionalRepositoryGateway profesionales, HorarioRepositoryGateway horarios) {
        this.profesionales = profesionales;
        this.horarios = horarios;
    }

    @Override
    @Transactional(readOnly = true)
    public HorarioAtencion[] ejecutar(String profesionalId) {
        if (profesionales.buscarPorId(profesionalId) == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        return horarios.listarDeProfesional(profesionalId);
    }
}
