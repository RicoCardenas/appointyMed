package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.port.in.ConsultarHistorialCitaPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.HistorialCitaRepositoryPort;

public class ConsultarHistorialCitaUseCase implements ConsultarHistorialCitaPort {

    private final CatalogClientPort catalog;
    private final CitaRepositoryPort citas;
    private final HistorialCitaRepositoryPort historial;

    public ConsultarHistorialCitaUseCase(CatalogClientPort catalog, CitaRepositoryPort citas, HistorialCitaRepositoryPort historial) {
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
