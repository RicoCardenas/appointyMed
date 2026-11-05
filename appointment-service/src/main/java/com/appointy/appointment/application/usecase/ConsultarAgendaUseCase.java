package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.port.in.ConsultarAgendaPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;

/** CU-22. Resuelve en catalog el profesional de la cuenta antes de abrir la consulta. */
public class ConsultarAgendaUseCase implements ConsultarAgendaPort {

    private final CatalogClientPort catalog;
    private final CitaRepositoryPort citas;
    private final FranjaRepositoryPort franjas;
    private final RelojClinica reloj;

    public ConsultarAgendaUseCase(CatalogClientPort catalog, CitaRepositoryPort citas, FranjaRepositoryPort franjas, RelojClinica reloj) {
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
