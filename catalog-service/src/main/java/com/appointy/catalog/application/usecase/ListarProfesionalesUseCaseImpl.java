package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.rule.ReglasConsulta;
import com.appointy.catalog.domain.usecase.ListarProfesionalesUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ListarProfesionalesUseCaseImpl implements ListarProfesionalesUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public ListarProfesionalesUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaProfesionales ejecutar(FiltroProfesionales filtro, int pagina, int tamano) {
        ReglasConsulta.validarPaginacion(pagina, tamano);
        return profesionales.listar(filtro, pagina, tamano);
    }
}
