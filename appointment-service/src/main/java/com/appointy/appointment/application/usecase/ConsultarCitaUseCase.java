package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.port.in.ConsultarCitaPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;

public class ConsultarCitaUseCase implements ConsultarCitaPort {

    private final CatalogClientPort catalog;
    private final CitaRepositoryPort citas;
    private final FranjaRepositoryPort franjas;

    public ConsultarCitaUseCase(CatalogClientPort catalog, CitaRepositoryPort citas, FranjaRepositoryPort franjas) {
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
