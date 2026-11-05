package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.usecase.ConsultarCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;

public class ConsultarCitaUseCaseImpl implements ConsultarCitaUseCase {

    private final CatalogClientGateway catalog;
    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;

    public ConsultarCitaUseCaseImpl(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas) {
        this.catalog = catalog;
        this.citas = citas;
        this.franjas = franjas;
    }

    @Override
    public CitaConFranja ejecutar(Actor actor, String citaId) {
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        AyudaCitas.exigirLectura(catalog, actor, cita);
        return new CitaConFranja(cita, AyudaCitas.buscarFranja(franjas, cita.franjaId()));
    }
}
