package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.gateway.RegistroGateway;
import com.appointy.catalog.domain.gateway.RelojGateway;
import com.appointy.catalog.domain.rule.Arreglos;
import com.appointy.catalog.domain.rule.FechaIso;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.rule.ReglasParametro;

/**
 * Pide a appointment las franjas de un horario hasta el horizonte de agendamiento. El horario ya esta guardado:
 * si appointment no responde, se informa y el administrativo reintenta (la generacion es idempotente, RNF-16).
 */
public class PublicadorDeFranjas {

    private final ParametroRepositoryGateway parametros;
    private final AppointmentClientGateway appointment;
    private final FestivosClientGateway festivos;
    private final RelojGateway reloj;
    private final RegistroGateway registro;

    public PublicadorDeFranjas(ParametroRepositoryGateway parametros, AppointmentClientGateway appointment, FestivosClientGateway festivos, RelojGateway reloj, RegistroGateway registro) {
        this.parametros = parametros;
        this.appointment = appointment;
        this.festivos = festivos;
        this.reloj = reloj;
        this.registro = registro;
    }

    public ResultadoHorario publicar(HorarioAtencion horario) {
        if (!horario.activo()) {
            return new ResultadoHorario(horario, false, 0, 0, "El horario esta inactivo: no se generan franjas");
        }
        String hoy = reloj.hoy();
        int horizonte = Parametros.entero(parametros, ReglasParametro.HORIZONTE_MAX_DIAS);
        String hasta = FechaIso.sumarDias(hoy, horizonte);
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario, hoy, hasta);
        String mensajeOk = "Franjas generadas";
        try {
            franjas = sinFestivos(franjas, anio(hoy), anio(hasta));
        } catch (ServicioNoDisponibleException e) {
            // RA-06: la API externa es opcional; sin ella se generan todas las franjas y el administrativo lo sabe.
            registro.info("Franjas del horario " + horario.id() + " sin filtrar festivos: " + e.codigo());
            mensajeOk = "Franjas generadas sin filtrar festivos (el servicio de festivos no respondio)";
        }
        try {
            ResultadoGeneracion r = appointment.generarFranjas(horario.profesionalId(), horario.centroId(), franjas);
            return new ResultadoHorario(horario, true, r.creadas(), r.existentes(), mensajeOk);
        } catch (ServicioNoDisponibleException e) {
            registro.info("Horario " + horario.id() + " guardado, pero appointment no genero las franjas: " + e.codigo());
            return new ResultadoHorario(horario, false, 0, 0,
                    "El horario se guardo, pero appointment no respondio: reintenta la generacion de franjas");
        }
    }

    private static int anio(String fechaIso) {
        return Integer.parseInt(fechaIso.substring(0, 4));
    }

    /** Retira las franjas que caen en un dia festivo. */
    private FranjaSolicitada[] sinFestivos(FranjaSolicitada[] franjas, int desdeAnio, int hastaAnio) {
        String[] dias = new String[0];
        for (int anio = desdeAnio; anio <= hastaAnio; anio++) {
            Festivo[] delAnio = festivos.festivos(anio);
            String[] nuevos = new String[dias.length + delAnio.length];
            System.arraycopy(dias, 0, nuevos, 0, dias.length);
            for (int i = 0; i < delAnio.length; i++) {
                nuevos[dias.length + i] = delAnio[i].fecha();
            }
            dias = nuevos;
        }
        FranjaSolicitada[] libres = new FranjaSolicitada[franjas.length];
        int n = 0;
        for (FranjaSolicitada f : franjas) {
            if (!Arreglos.contiene(dias, f.fecha())) {
                libres[n++] = f;
            }
        }
        FranjaSolicitada[] recortadas = new FranjaSolicitada[n];
        System.arraycopy(libres, 0, recortadas, 0, n);
        return recortadas;
    }
}
