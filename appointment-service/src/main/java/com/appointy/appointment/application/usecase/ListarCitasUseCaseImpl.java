package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.PaginaCitas;
import com.appointy.appointment.domain.model.PaginaCitasConFranja;
import com.appointy.appointment.domain.rule.ReglasCita;
import com.appointy.appointment.domain.usecase.ListarCitasUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ListarCitasUseCaseImpl implements ListarCitasUseCase {

    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;

    public ListarCitasUseCaseImpl(CitaRepositoryGateway citas, FranjaRepositoryGateway franjas) {
        this.citas = citas;
        this.franjas = franjas;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaCitasConFranja ejecutar(Actor actor, FiltroCitas filtro, int pagina, int tamano) {
        if (!actor.esAdministrativo()) {
            throw new AccesoProhibidoException("SOLO_ADMINISTRATIVO", "Solo el personal administrativo lista todas las citas");
        }
        ReglasCita.validarPaginacion(pagina, tamano);
        PaginaCitas p = citas.listar(filtro, pagina, tamano);
        return new PaginaCitasConFranja(AyudaCitas.conFranjas(franjas, p.contenido()), p.pagina(), p.tamano(), p.totalElementos(), p.totalPaginas());
    }
}
