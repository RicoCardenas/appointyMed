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
import com.appointy.appointment.domain.usecase.CitaUseCase.CancelarCitaComando;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

public class CancelarCitaTransaccion {

    private final FranjaRepositoryGateway franjas;
    private final CitaRepositoryGateway citas;
    private final HistorialCitaRepositoryGateway historial;
    private final PublicadorEventosGateway eventos;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public CancelarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial,
            PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    public CitaConFranja cancelar(CancelarCitaComando comando, String franjaId, ParametrosCita parametros) {
        return transacciones.ejecutar(() -> {
            Franja franja = AyudaCitas.buscarFranjaParaActualizar(franjas, franjaId);
            Cita cita = AyudaCitas.cargarCitaBajoBloqueo(citas, comando.citaId(), franjaId);
            ReglasCita.exigirDuenioOAdministrativo(cita, comando.actor());
            ReglasCita.exigirConfirmada(cita);
            ReglasCita.exigirDentroDelLimiteDeCancelacion(FechaIso.minutosLocales(franja.fecha(), franja.horaInicio()),
                    reloj.ahoraMinutosLocales(), parametros.limiteCancelacionHoras());

            long ahora = reloj.ahoraMillis();
            Franja libre = franjas.guardar(franja.liberar());
            Cita cancelada = citas.guardar(cita.conEstado(EstadoCita.CANCELADA, ahora));
            historial.registrar(new HistorialCita(ids.nuevoId(), cita.id(), EstadoCita.CONFIRMADA, EstadoCita.CANCELADA,
                    comando.actor().usuarioId(), comando.actor().rol(), franja.id(), null,
                    comando.motivo() == null || comando.motivo().isBlank() ? "Cita cancelada" : "Cita cancelada: " + comando.motivo(), ahora));
            eventos.publicar(new EventoCita("CANCELADA", cita.id(), cita.pacienteId(), cita.profesionalId(), cita.centroId(), franja.fecha(),
                    franja.horaInicio(), comando.actor().usuarioId()));
            return new CitaConFranja(cancelada, libre);
        });
    }
}
