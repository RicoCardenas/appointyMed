package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.usecase.ConsultarAgendaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;

/** CU-22. Resuelve en catalog el profesional de la cuenta antes de abrir la consulta. */
public class ConsultarAgendaUseCaseImpl implements ConsultarAgendaUseCase {

    private final CatalogClientGateway catalog;
    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;
    private final RelojClinica reloj;

    public ConsultarAgendaUseCaseImpl(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, RelojClinica reloj) {
        this.catalog = catalog;
        this.citas = citas;
        this.franjas = franjas;
        this.reloj = reloj;
    }

    @Override
    public CitaConFranja[] ejecutar(String usuarioId, String fecha) {
        String profesionalId = AyudaCitas.profesionalDe(catalog, new Actor(usuarioId, "PROFESIONAL"));
        String desde = fecha != null ? fecha : reloj.hoy();
        String hasta = fecha != null ? fecha : RelojClinica.FECHA_MAXIMA;
        return AyudaCitas.conFranjas(franjas, citas.listarAgendaConfirmada(profesionalId, desde, hasta));
    }
}
