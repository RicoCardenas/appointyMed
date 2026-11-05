package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.usecase.ConsultarDisponibilidadUseCase;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarDisponibilidadUseCaseImpl implements ConsultarDisponibilidadUseCase {

    private final FranjaRepositoryGateway franjas;
    private final RelojClinica reloj;

    public ConsultarDisponibilidadUseCaseImpl(FranjaRepositoryGateway franjas, RelojClinica reloj) {
        this.franjas = franjas;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public Franja[] ejecutar(String profesionalId, String centroId, String fecha) {
        long ahora = reloj.ahoraMinutosLocales();
        List<Franja> futuras = new ArrayList<>();
        for (Franja f : franjas.listarDisponibles(profesionalId, centroId, fecha)) {
            if (RelojClinica.minutosLocales(f.fecha(), f.horaInicio()) > ahora) {
                futuras.add(f);
            }
        }
        return futuras.toArray(new Franja[0]);
    }
}
