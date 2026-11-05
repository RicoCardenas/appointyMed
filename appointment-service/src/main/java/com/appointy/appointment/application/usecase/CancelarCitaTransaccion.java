package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.port.in.CancelarCitaPort.Comando;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import com.appointy.appointment.domain.port.out.HistorialCitaRepositoryPort;
import com.appointy.appointment.domain.port.out.PublicadorEventosPort;
import com.appointy.appointment.domain.rule.ReglasCita;
import org.springframework.transaction.annotation.Transactional;

public class CancelarCitaTransaccion {

    private final FranjaRepositoryPort franjas;
    private final CitaRepositoryPort citas;
    private final HistorialCitaRepositoryPort historial;
    private final PublicadorEventosPort eventos;
    private final RelojClinica reloj;

    public CancelarCitaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas, HistorialCitaRepositoryPort historial,
            PublicadorEventosPort eventos, RelojClinica reloj) {
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Transactional
    public CitaConFranja cancelar(Comando comando, String franjaId, ParametrosCita parametros) {
        Franja franja = AyudaCitas.buscarFranjaParaActualizar(franjas, franjaId);
        Cita cita = AyudaCitas.cargarCitaBajoBloqueo(citas, comando.citaId(), franjaId);
        ReglasCita.exigirDuenioOAdministrativo(cita, comando.actor());
        ReglasCita.exigirConfirmada(cita);
        ReglasCita.exigirDentroDelLimiteDeCancelacion(RelojClinica.minutosLocales(franja.fecha(), franja.horaInicio()),
                reloj.ahoraMinutosLocales(), parametros.limiteCancelacionHoras());

        long ahora = reloj.ahoraMillis();
        Franja libre = franjas.guardar(franja.liberar());
        Cita cancelada = citas.guardar(cita.conEstado(EstadoCita.CANCELADA, ahora));
        historial.registrar(new HistorialCita(AyudaCitas.nuevoId(), cita.id(), EstadoCita.CONFIRMADA, EstadoCita.CANCELADA,
                comando.actor().usuarioId(), comando.actor().rol(), franja.id(), null,
                comando.motivo() == null || comando.motivo().isBlank() ? "Cita cancelada" : "Cita cancelada: " + comando.motivo(), ahora));
        eventos.publicar(new EventoCita("CANCELADA", cita.id(), cita.pacienteId(), cita.profesionalId(), cita.centroId(), franja.fecha(),
                franja.horaInicio(), comando.actor().usuarioId()));
        return new CitaConFranja(cancelada, libre);
    }
}
