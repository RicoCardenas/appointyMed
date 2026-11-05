package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.port.in.ListarHorariosPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ListarHorariosUseCase implements ListarHorariosPort {

    private final ProfesionalRepositoryPort profesionales;
    private final HorarioRepositoryPort horarios;

    public ListarHorariosUseCase(ProfesionalRepositoryPort profesionales, HorarioRepositoryPort horarios) {
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
