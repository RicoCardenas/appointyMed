package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.usecase.MarcarCitaAtendidaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/** CU-23. */
public class MarcarCitaAtendidaUseCaseImpl implements MarcarCitaAtendidaUseCase {

    private final CatalogClientGateway catalog;
    private final CitaRepositoryGateway citas;
    private final MarcarCitaAtendidaTransaccion transaccion;

    public MarcarCitaAtendidaUseCaseImpl(CatalogClientGateway catalog, CitaRepositoryGateway citas, MarcarCitaAtendidaTransaccion transaccion) {
        this.catalog = catalog;
        this.citas = citas;
        this.transaccion = transaccion;
    }

    @Override
    public CitaConFranja ejecutar(Actor actor, String citaId, EstadoCita nuevoEstado) {
        ReglasCita.exigirEstadoDeAtencion(nuevoEstado);
        String profesionalId = AyudaCitas.profesionalDe(catalog, actor);
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        ReglasCita.exigirProfesionalDeLaCita(cita, profesionalId);
        ReglasCita.exigirConfirmada(cita);
        return transaccion.marcar(actor, citaId, cita.franjaId(), profesionalId, nuevoEstado);
    }
}
