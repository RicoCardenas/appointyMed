package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.usecase.ConsultarHistorialCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;

public class ConsultarHistorialCitaUseCaseImpl implements ConsultarHistorialCitaUseCase {

    private final CatalogClientGateway catalog;
    private final CitaRepositoryGateway citas;
    private final HistorialCitaRepositoryGateway historial;

    public ConsultarHistorialCitaUseCaseImpl(CatalogClientGateway catalog, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial) {
        this.catalog = catalog;
        this.citas = citas;
        this.historial = historial;
    }

    @Override
    public HistorialCita[] ejecutar(Actor actor, String citaId) {
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        AyudaCitas.exigirLectura(catalog, actor, cita);
        return historial.listarPorCita(citaId);
    }
}
