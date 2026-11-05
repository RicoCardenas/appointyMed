package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.FranjaNueva;
import com.appointy.appointment.domain.model.ResultadoGeneracion;
import com.appointy.appointment.domain.usecase.GenerarFranjasUseCase;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

/** CU-25. Repetir la misma peticion no duplica nada: la base ignora la franja que ya existe (RNF-16). */
public class GenerarFranjasUseCaseImpl implements GenerarFranjasUseCase {

    private static final int MINUTOS_POR_DIA = 24 * 60;

    private final FranjaRepositoryGateway franjas;

    public GenerarFranjasUseCaseImpl(FranjaRepositoryGateway franjas) {
        this.franjas = franjas;
    }

    @Override
    @Transactional
    public ResultadoGeneracion ejecutar(String profesionalId, String centroId, FranjaNueva[] solicitadas) {
        for (FranjaNueva f : solicitadas) {
            if (f.horaInicio() < 0 || f.horaFin() >= MINUTOS_POR_DIA || f.horaFin() <= f.horaInicio()) {
                throw new ValidacionException("FRANJA_INVALIDA", "La hora de fin debe ser posterior a la de inicio, dentro del mismo dia");
            }
        }
        int creadas = 0;
        int existentes = 0;
        for (FranjaNueva f : solicitadas) {
            Franja franja = new Franja(AyudaCitas.nuevoId(), profesionalId, centroId, f.fecha(), f.horaInicio(), f.horaFin(),
                    EstadoFranja.DISPONIBLE, null);
            if (franjas.insertarSiNoExiste(franja)) {
                creadas++;
            } else {
                existentes++;
            }
        }
        return new ResultadoGeneracion(creadas, existentes);
    }
}
