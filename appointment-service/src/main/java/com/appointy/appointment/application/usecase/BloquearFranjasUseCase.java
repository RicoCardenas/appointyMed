package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.ConflictoCitasException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.port.in.BloquearFranjasPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-26. Bloquea las franjas del rango antes de mirar las citas, asi una reserva en curso termina antes o espera;
 * si hay citas CONFIRMADAS no cambia nada (RN-11).
 */
public class BloquearFranjasUseCase implements BloquearFranjasPort {

    private final FranjaRepositoryPort franjas;
    private final CitaRepositoryPort citas;

    public BloquearFranjasUseCase(FranjaRepositoryPort franjas, CitaRepositoryPort citas) {
        this.franjas = franjas;
        this.citas = citas;
    }

    @Override
    @Transactional
    public int ejecutar(String profesionalId, String centroId, String fechaInicio, String fechaFin) {
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
    }
}
