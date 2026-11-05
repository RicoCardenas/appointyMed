package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.port.in.ConsultarMisCitasPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarMisCitasUseCase implements ConsultarMisCitasPort {

    private final CitaRepositoryPort citas;
    private final FranjaRepositoryPort franjas;
    private final RelojClinica reloj;

    public ConsultarMisCitasUseCase(CitaRepositoryPort citas, FranjaRepositoryPort franjas, RelojClinica reloj) {
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
