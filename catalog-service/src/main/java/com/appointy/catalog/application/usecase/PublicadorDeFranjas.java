package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.rule.ReglasParametro;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pide a appointment las franjas de un horario hasta el horizonte de agendamiento. El horario ya esta guardado:
 * si appointment no responde, se informa y el administrativo reintenta (la generacion es idempotente, RNF-16).
 */
public class PublicadorDeFranjas {

    private static final Logger LOG = LoggerFactory.getLogger(PublicadorDeFranjas.class);

    private final ParametroRepositoryGateway parametros;
    private final AppointmentClientGateway appointment;
    private final Clock reloj;

    public PublicadorDeFranjas(ParametroRepositoryGateway parametros, AppointmentClientGateway appointment, Clock reloj) {
        this.parametros = parametros;
        this.appointment = appointment;
        this.reloj = reloj;
    }

    public ResultadoHorario publicar(HorarioAtencion horario) {
        if (!horario.activo()) {
            return new ResultadoHorario(horario, false, 0, 0, "El horario esta inactivo: no se generan franjas");
        }
        LocalDate hoy = LocalDate.now(reloj);
        int horizonte = Parametros.entero(parametros, ReglasParametro.HORIZONTE_MAX_DIAS);
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario, hoy, hoy.plusDays(horizonte));
        try {
            ResultadoGeneracion r = appointment.generarFranjas(horario.profesionalId(), horario.centroId(), franjas);
            return new ResultadoHorario(horario, true, r.creadas(), r.existentes(), "Franjas generadas");
        } catch (ServicioNoDisponibleException e) {
            LOG.warn("Horario {} guardado, pero appointment no genero las franjas: {}", horario.id(), e.codigo());
            return new ResultadoHorario(horario, false, 0, 0,
                    "El horario se guardo, pero appointment no respondio: reintenta la generacion de franjas");
        }
    }
}
