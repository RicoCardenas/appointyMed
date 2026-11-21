package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.exception.ReglaNegocioException;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.usecase.CitaUseCase.ReprogramarCitaComando;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/**
 * Ocupa la nueva franja, libera la anterior, actualiza la cita y registra el historial, todo atomico. Orden de bloqueos:
 * paciente, luego las dos franjas por id ascendente (evita deadlocks entre reprogramaciones cruzadas), luego la cita.
 */
public class ReprogramarCitaTransaccion {

    private final FranjaRepositoryGateway franjas;
    private final CitaRepositoryGateway citas;
    private final HistorialCitaRepositoryGateway historial;
    private final PublicadorEventosGateway eventos;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public ReprogramarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial,
            PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.franjas = franjas;
        this.citas = citas;
        this.historial = historial;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    public CitaConFranja reprogramar(ReprogramarCitaComando comando, String franjaAnteriorId, ParametrosCita parametros) {
        return transacciones.ejecutar(() -> {
            String pacienteId = comando.actor().usuarioId();
            String nuevaId = comando.nuevaFranjaId();
            if (franjaAnteriorId.equals(nuevaId)) {
                throw new ReglaNegocioException("MISMA_FRANJA", "La nueva franja es la misma que ya tiene la cita");
            }
            citas.bloquearPaciente(pacienteId);
            boolean anteriorPrimero = franjaAnteriorId.compareTo(nuevaId) < 0;
            Franja primera = AyudaCitas.buscarFranjaParaActualizar(franjas, anteriorPrimero ? franjaAnteriorId : nuevaId);
            Franja segunda = AyudaCitas.buscarFranjaParaActualizar(franjas, anteriorPrimero ? nuevaId : franjaAnteriorId);
            Franja anterior = anteriorPrimero ? primera : segunda;
            Franja nueva = anteriorPrimero ? segunda : primera;

            Cita cita = AyudaCitas.cargarCitaBajoBloqueo(citas, comando.citaId(), franjaAnteriorId);
            ReglasCita.exigirPacienteDuenio(cita, comando.actor());
            ReglasCita.exigirConfirmada(cita);

            long ahoraMin = reloj.ahoraMinutosLocales();
            ReglasCita.exigirAnticipacionParaReprogramar(
                    FechaIso.minutosLocales(anterior.fecha(), anterior.horaInicio()), ahoraMin, parametros.anticipacionMinimaHoras());
            String fechaLimite = FechaIso.sumarDias(reloj.hoy(), parametros.horizonteMaxDias());
            ReglasCita.exigirFranjaReservable(nueva, FechaIso.minutosLocales(nueva.fecha(), nueva.horaInicio()), ahoraMin,
                    parametros.anticipacionMinimaHoras(), fechaLimite);
            Franja[] existentes = franjas.franjasDeCitasConfirmadasDelPaciente(pacienteId, nueva.fecha());
            ReglasCita.exigirSinCruce(nueva, sinLaAnterior(existentes, anterior.id()));

            long ahora = reloj.ahoraMillis();
            Franja ocupada = franjas.guardar(nueva.ocupar());
            franjas.guardar(anterior.liberar());
            Cita actualizada = citas.guardar(cita.reprogramada(nueva, ahora));
            historial.registrar(new HistorialCita(ids.nuevoId(), cita.id(), EstadoCita.CONFIRMADA, EstadoCita.CONFIRMADA,
                    pacienteId, comando.actor().rol(), anterior.id(), nueva.id(), "Cita reprogramada", ahora));
            eventos.publicar(new EventoCita("REPROGRAMADA", cita.id(), pacienteId, actualizada.profesionalId(), actualizada.centroId(),
                    nueva.fecha(), nueva.horaInicio(), pacienteId));
            return new CitaConFranja(actualizada, ocupada);
        });
    }

    private static Franja[] sinLaAnterior(Franja[] franjas, String anteriorId) {
        int cuantas = 0;
        for (Franja f : franjas) {
            if (!f.id().equals(anteriorId)) {
                cuantas++;
            }
        }
        Franja[] resto = new Franja[cuantas];
        int i = 0;
        for (Franja f : franjas) {
            if (!f.id().equals(anteriorId)) {
                resto[i++] = f;
            }
        }
        return resto;
    }
}
