package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.usecase.CitaUseCase.ReservarCitaComando;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/** Pasos 4 a 7 de CU-18 en un unico {@code @Transactional}: candado del paciente, franja PESSIMISTIC_WRITE, reglas, escritura. */
public class ReservarCitaTransaccion {

    private final FranjaRepositoryGateway franjas;
    private final CitaRepositoryGateway citas;
    private final HistorialCitaRepositoryGateway historial;
    private final PublicadorEventosGateway eventos;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public ReservarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial,
            PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    public CitaConFranja confirmar(ReservarCitaComando comando, ParametrosCita parametros) {
        return transacciones.ejecutar(() -> {
            String pacienteId = comando.actor().usuarioId();
            citas.bloquearPaciente(pacienteId);
            Franja franja = AyudaCitas.buscarFranjaParaActualizar(franjas, comando.franjaId());

            String fechaLimite = FechaIso.sumarDias(reloj.hoy(), parametros.horizonteMaxDias());
            ReglasCita.exigirFranjaDelProfesional(franja, comando.profesionalId(), comando.centroId());
            ReglasCita.exigirFranjaReservable(franja, FechaIso.minutosLocales(franja.fecha(), franja.horaInicio()),
                    reloj.ahoraMinutosLocales(), parametros.anticipacionMinimaHoras(), fechaLimite);
            ReglasCita.exigirSinCruce(franja, franjas.franjasDeCitasConfirmadasDelPaciente(pacienteId, franja.fecha()));

            long ahora = reloj.ahoraMillis();
            Franja ocupada = franjas.guardar(franja.ocupar());
            Cita cita = citas.guardar(new Cita(ids.nuevoId(), pacienteId, franja.profesionalId(), comando.especialidadId(),
                    franja.centroId(), franja.id(), EstadoCita.CONFIRMADA, ahora, ahora, null));
            historial.registrar(new HistorialCita(ids.nuevoId(), cita.id(), null, EstadoCita.CONFIRMADA, pacienteId,
                    comando.actor().rol(), null, franja.id(), "Cita reservada", ahora));
            eventos.publicar(new EventoCita("CONFIRMADA", cita.id(), pacienteId, cita.profesionalId(), cita.centroId(), franja.fecha(),
                    franja.horaInicio(), pacienteId));
            return new CitaConFranja(cita, ocupada);
        });
    }
}
