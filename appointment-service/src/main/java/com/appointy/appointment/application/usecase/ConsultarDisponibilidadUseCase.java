package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.port.in.ConsultarDisponibilidadPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarDisponibilidadUseCase implements ConsultarDisponibilidadPort {

    private final FranjaRepositoryPort franjas;
    private final RelojClinica reloj;

    public ConsultarDisponibilidadUseCase(FranjaRepositoryPort franjas, RelojClinica reloj) {
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
