package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.rule.ReglasCita;
import org.springframework.transaction.annotation.Transactional;

/** La franja sigue OCUPADA: la cita ya se atendio (o el paciente no vino) y ese horario no vuelve a ofrecerse. */
public class MarcarCitaAtendidaTransaccion {

    private final FranjaRepositoryGateway franjas;
    private final CitaRepositoryGateway citas;
    private final HistorialCitaRepositoryGateway historial;
    private final RelojClinica reloj;

    public MarcarCitaAtendidaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial,
            RelojClinica reloj) {
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.reloj = reloj;
    }

    @Transactional
    public CitaConFranja marcar(Actor actor, String citaId, String franjaId, String profesionalId, EstadoCita nuevoEstado) {
        Franja franja = AyudaCitas.buscarFranjaParaActualizar(franjas, franjaId);
        Cita cita = AyudaCitas.cargarCitaBajoBloqueo(citas, citaId, franjaId);
        ReglasCita.exigirProfesionalDeLaCita(cita, profesionalId);
        ReglasCita.exigirConfirmada(cita);
        long ahora = reloj.ahoraMillis();
        Cita actualizada = citas.guardar(cita.conEstado(nuevoEstado, ahora));
        historial.registrar(new HistorialCita(AyudaCitas.nuevoId(), cita.id(), EstadoCita.CONFIRMADA, nuevoEstado, actor.usuarioId(),
                actor.rol(), franjaId, null, "Cita marcada " + nuevoEstado, ahora));
        return new CitaConFranja(actualizada, franja);
    }
}
