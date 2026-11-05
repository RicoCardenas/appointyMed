package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.port.in.ReservarCitaPort.Comando;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import com.appointy.appointment.domain.port.out.HistorialCitaRepositoryPort;
import com.appointy.appointment.domain.port.out.PublicadorEventosPort;
import com.appointy.appointment.domain.rule.ReglasCita;
import org.springframework.transaction.annotation.Transactional;

/** Pasos 4 a 7 de CU-18 en un unico {@code @Transactional}: candado del paciente, franja PESSIMISTIC_WRITE, reglas, escritura. */
public class ReservarCitaTransaccion {

    private final FranjaRepositoryPort franjas;
    private final CitaRepositoryPort citas;
    private final HistorialCitaRepositoryPort historial;
    private final PublicadorEventosPort eventos;
    private final RelojClinica reloj;

    public ReservarCitaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas, HistorialCitaRepositoryPort historial,
            PublicadorEventosPort eventos, RelojClinica reloj) {
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Transactional
    public CitaConFranja confirmar(Comando comando, ParametrosCita parametros) {
        String pacienteId = comando.actor().usuarioId();
        citas.bloquearPaciente(pacienteId);
        Franja franja = AyudaCitas.buscarFranjaParaActualizar(franjas, comando.franjaId());

        String fechaLimite = reloj.sumarDias(reloj.hoy(), parametros.horizonteMaxDias());
        ReglasCita.exigirFranjaDelProfesional(franja, comando.profesionalId(), comando.centroId());
        ReglasCita.exigirFranjaReservable(franja, RelojClinica.minutosLocales(franja.fecha(), franja.horaInicio()),
                reloj.ahoraMinutosLocales(), parametros.anticipacionMinimaHoras(), fechaLimite);
        ReglasCita.exigirSinCruce(franja, franjas.franjasDeCitasConfirmadasDelPaciente(pacienteId, franja.fecha()));

        long ahora = reloj.ahoraMillis();
        Franja ocupada = franjas.guardar(franja.ocupar());
        Cita cita = citas.guardar(new Cita(AyudaCitas.nuevoId(), pacienteId, franja.profesionalId(), comando.especialidadId(),
                franja.centroId(), franja.id(), EstadoCita.CONFIRMADA, ahora, ahora, null));
        historial.registrar(new HistorialCita(AyudaCitas.nuevoId(), cita.id(), null, EstadoCita.CONFIRMADA, pacienteId,
                comando.actor().rol(), null, franja.id(), "Cita reservada", ahora));
        eventos.publicar(new EventoCita("CONFIRMADA", cita.id(), pacienteId, cita.profesionalId(), cita.centroId(), franja.fecha(),
                franja.horaInicio(), pacienteId));
        return new CitaConFranja(cita, ocupada);
    }
}
