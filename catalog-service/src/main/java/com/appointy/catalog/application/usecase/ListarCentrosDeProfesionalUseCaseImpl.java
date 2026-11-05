package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.usecase.ListarCentrosDeProfesionalUseCase;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ListarCentrosDeProfesionalUseCaseImpl implements ListarCentrosDeProfesionalUseCase {

    private final ProfesionalRepositoryGateway profesionales;
    private final CentroRepositoryGateway centros;

    public ListarCentrosDeProfesionalUseCaseImpl(ProfesionalRepositoryGateway profesionales, CentroRepositoryGateway centros) {
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
