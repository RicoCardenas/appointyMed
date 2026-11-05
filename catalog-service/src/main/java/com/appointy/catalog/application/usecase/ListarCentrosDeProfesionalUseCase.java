package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.port.in.ListarCentrosDeProfesionalPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ListarCentrosDeProfesionalUseCase implements ListarCentrosDeProfesionalPort {

    private final ProfesionalRepositoryPort profesionales;
    private final CentroRepositoryPort centros;

    public ListarCentrosDeProfesionalUseCase(ProfesionalRepositoryPort profesionales, CentroRepositoryPort centros) {
        this.profesionales = profesionales;
        this.centros = centros;
    }

    @Override
    @Transactional(readOnly = true)
    public Centro[] ejecutar(String profesionalId) {
        if (profesionales.buscarPorId(profesionalId) == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        return centros.listarActivosDeProfesional(profesionalId);
    }
}
