package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import com.appointy.appointment.domain.port.out.HistorialCitaRepositoryPort;
import com.appointy.appointment.domain.rule.ReglasCita;
import org.springframework.transaction.annotation.Transactional;

/** La franja sigue OCUPADA: la cita ya se atendio (o el paciente no vino) y ese horario no vuelve a ofrecerse. */
public class MarcarCitaAtendidaTransaccion {

    private final FranjaRepositoryPort franjas;
    private final CitaRepositoryPort citas;
    private final HistorialCitaRepositoryPort historial;
    private final RelojClinica reloj;

    public MarcarCitaAtendidaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas, HistorialCitaRepositoryPort historial,
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
