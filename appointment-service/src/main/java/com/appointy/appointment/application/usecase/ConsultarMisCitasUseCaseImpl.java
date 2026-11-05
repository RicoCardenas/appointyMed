package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.usecase.ConsultarMisCitasUseCase;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarMisCitasUseCaseImpl implements ConsultarMisCitasUseCase {

    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;
    private final RelojClinica reloj;

    public ConsultarMisCitasUseCaseImpl(CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, RelojClinica reloj) {
        this.citas = citas;
        this.franjas = franjas;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public CitaConFranja[] ejecutar(String pacienteId, String periodo) {
        String hoy = reloj.hoy();
        boolean proximas = "PROXIMAS".equals(periodo);
        String desde = RelojClinica.FECHA_MINIMA;
        String hasta = RelojClinica.FECHA_MAXIMA;
        if (proximas) {
            desde = hoy;
        } else if ("PASADAS".equals(periodo)) {
            hasta = reloj.sumarDias(hoy, -1);
        } else if (periodo != null) {
            throw new ValidacionException("PERIODO_INVALIDO", "El periodo debe ser PROXIMAS o PASADAS");
        }
        Cita[] encontradas = citas.listarDePaciente(pacienteId, desde, hasta);
        CitaConFranja[] resultado = AyudaCitas.conFranjas(franjas, encontradas);
        if (!proximas) {
            for (int i = 0, j = resultado.length - 1; i < j; i++, j--) {
                CitaConFranja t = resultado[i];
                resultado[i] = resultado[j];
                resultado[j] = t;
            }
        }
        return resultado;
    }
}
