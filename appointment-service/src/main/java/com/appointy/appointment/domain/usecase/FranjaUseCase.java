package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.exception.ConflictoCitasException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.FranjaNueva;
import com.appointy.appointment.domain.model.ResultadoGeneracion;

/** generar franjas, bloquear franjas, consultar disponibilidad. */
public class FranjaUseCase {

    private static final int MINUTOS_POR_DIA = 24 * 60;

    private final FranjaRepositoryGateway franjas;
    private final CitaRepositoryGateway citas;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public FranjaUseCase(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.franjas = franjas;
        this.citas = citas;
        this.reloj = reloj;
    }

    /** CU-25: idempotente (RNF-16). */
    public ResultadoGeneracion generarFranjas(String profesionalId, String centroId, FranjaNueva[] solicitadas) {
        return transacciones.ejecutar(() -> {
            for (FranjaNueva f : solicitadas) {
                if (f.horaInicio() < 0 || f.horaFin() >= MINUTOS_POR_DIA || f.horaFin() <= f.horaInicio()) {
                    throw new ValidacionException("FRANJA_INVALIDA", "La hora de fin debe ser posterior a la de inicio, dentro del mismo dia");
                }
            }
            int creadas = 0;
            int existentes = 0;
            for (FranjaNueva f : solicitadas) {
                Franja franja = new Franja(ids.nuevoId(), profesionalId, centroId, f.fecha(), f.horaInicio(), f.horaFin(),
                        EstadoFranja.DISPONIBLE, null);
                if (franjas.insertarSiNoExiste(franja)) {
                    creadas++;
                } else {
                    existentes++;
                }
            }
            return new ResultadoGeneracion(creadas, existentes);
        });
    }

    /** CU-26: devuelve cuantas franjas pasaron a BLOQUEADA; lanza ConflictoCitasException si hay citas CONFIRMADAS. Idempotente. */
    public int bloquearFranjas(String profesionalId, String centroId, String fechaInicio, String fechaFin) {
        return transacciones.ejecutar(() -> {
            if (fechaFin.compareTo(fechaInicio) < 0) {
                throw new ValidacionException("RANGO_INVALIDO", "La fecha final no puede ser anterior a la inicial");
            }
            Franja[] enRango = franjas.bloquearRango(profesionalId, centroId, fechaInicio, fechaFin);
            String[] afectadas = citas.idsConfirmadasEnRango(profesionalId, centroId, fechaInicio, fechaFin);
            if (afectadas.length > 0) {
                throw new ConflictoCitasException(afectadas);
            }
            int bloqueadas = 0;
            for (Franja f : enRango) {
                if (f.estado() == EstadoFranja.DISPONIBLE) {
                    franjas.guardar(f.bloquear());
                    bloqueadas++;
                }
            }
            return bloqueadas;
        });
    }

    /** CU-17: franjas DISPONIBLES y futuras de un profesional en un centro y fecha (ISO). */
    public Franja[] consultarDisponibilidad(String profesionalId, String centroId, String fecha) {
        return transacciones.ejecutarSoloLectura(() -> {
            long ahora = reloj.ahoraMinutosLocales();
            Franja[] disponibles = franjas.listarDisponibles(profesionalId, centroId, fecha);
            Franja[] futuras = new Franja[disponibles.length];
            int n = 0;
            for (Franja f : disponibles) {
                if (FechaIso.minutosLocales(f.fecha(), f.horaInicio()) > ahora) {
                    futuras[n++] = f;
                }
            }
            Franja[] resultado = new Franja[n];
            System.arraycopy(futuras, 0, resultado, 0, n);
            return resultado;
        });
    }
}
