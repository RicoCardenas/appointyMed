package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.rule.ReglasParametro;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final FestivosClientGateway festivos;
    private final Clock reloj;

    public PublicadorDeFranjas(ParametroRepositoryGateway parametros, AppointmentClientGateway appointment, FestivosClientGateway festivos, Clock reloj) {
        this.parametros = parametros;
        this.appointment = appointment;
        this.festivos = festivos;
        this.reloj = reloj;
    }

    public ResultadoHorario publicar(HorarioAtencion horario) {
        if (!horario.activo()) {
            return new ResultadoHorario(horario, false, 0, 0, "El horario esta inactivo: no se generan franjas");
        }
        LocalDate hoy = LocalDate.now(reloj);
        int horizonte = Parametros.entero(parametros, ReglasParametro.HORIZONTE_MAX_DIAS);
        LocalDate hasta = hoy.plusDays(horizonte);
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario, hoy, hasta);
        String mensajeOk = "Franjas generadas";
        try {
            franjas = sinFestivos(franjas, hoy.getYear(), hasta.getYear());
        } catch (ServicioNoDisponibleException e) {
            // RA-06: la API externa es opcional; sin ella se generan todas las franjas y el administrativo lo sabe.
            LOG.warn("Franjas del horario {} sin filtrar festivos: {}", horario.id(), e.codigo());
            mensajeOk = "Franjas generadas sin filtrar festivos (el servicio de festivos no respondio)";
        }
        try {
            ResultadoGeneracion r = appointment.generarFranjas(horario.profesionalId(), horario.centroId(), franjas);
            return new ResultadoHorario(horario, true, r.creadas(), r.existentes(), mensajeOk);
        } catch (ServicioNoDisponibleException e) {
            LOG.warn("Horario {} guardado, pero appointment no genero las franjas: {}", horario.id(), e.codigo());
            return new ResultadoHorario(horario, false, 0, 0,
                    "El horario se guardo, pero appointment no respondio: reintenta la generacion de franjas");
        }
    }

    /** Retira las franjas que caen en un dia festivo. */
    private FranjaSolicitada[] sinFestivos(FranjaSolicitada[] franjas, int desdeAnio, int hastaAnio) {
        Set<String> dias = new HashSet<>();
        for (int anio = desdeAnio; anio <= hastaAnio; anio++) {
            for (Festivo f : festivos.festivos(anio)) {
                dias.add(f.fecha());
            }
        }
        List<FranjaSolicitada> libres = new ArrayList<>();
        for (FranjaSolicitada f : franjas) {
            if (!dias.contains(f.fecha())) {
                libres.add(f);
            }
        }
        return libres.toArray(new FranjaSolicitada[0]);
    }
}
