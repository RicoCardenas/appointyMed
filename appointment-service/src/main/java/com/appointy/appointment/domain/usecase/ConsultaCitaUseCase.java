package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.PaginaCitas;
import com.appointy.appointment.domain.model.PaginaCitasConFranja;
import com.appointy.appointment.domain.rule.ReglasCita;

/** consultar cita, consultar mis citas, consultar agenda, listar citas, consultar historial cita, consultar estadisticas citas, obtener cita servicio, validar relacion. */
public class ConsultaCitaUseCase {

    private final CatalogClientGateway catalog;
    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;
    private final RelojGateway reloj;
    private final HistorialCitaRepositoryGateway historial;
    private final TransactionGateway transacciones;

    public ConsultaCitaUseCase(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, RelojGateway reloj, HistorialCitaRepositoryGateway historial, TransactionGateway transacciones) {
        this.transacciones = transacciones;
        this.catalog = catalog;
        this.citas = citas;
        this.franjas = franjas;
        this.reloj = reloj;
        this.historial = historial;
    }

    public CitaConFranja consultarCita(Actor actor, String citaId) {
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        AyudaCitas.exigirLectura(catalog, actor, cita);
        return new CitaConFranja(cita, AyudaCitas.buscarFranja(franjas, cita.franjaId()));
    }

    /** CU-21. {@code periodo}: PROXIMAS, PASADAS o null para todas. Proximas de la mas cercana a la mas lejana; el resto, de la mas reciente a la mas antigua. */
    public CitaConFranja[] consultarMisCitas(String pacienteId, String periodo) {
        return transacciones.ejecutarSoloLectura(() -> {
            String hoy = reloj.hoy();
            boolean proximas = "PROXIMAS".equals(periodo);
            String desde = FechaIso.FECHA_MINIMA;
            String hasta = FechaIso.FECHA_MAXIMA;
            if (proximas) {
                desde = hoy;
            } else if ("PASADAS".equals(periodo)) {
                hasta = FechaIso.sumarDias(hoy, -1);
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
        });
    }

    /** CU-22: citas CONFIRMADAS del profesional. Con {@code fecha} (ISO) solo ese dia; con null, de hoy en adelante. */
    public CitaConFranja[] consultarAgenda(String usuarioId, String fecha) {
        String profesionalId = AyudaCitas.profesionalDe(catalog, new Actor(usuarioId, "PROFESIONAL"));
        String desde = fecha != null ? fecha : reloj.hoy();
        String hasta = fecha != null ? fecha : FechaIso.FECHA_MAXIMA;
        return AyudaCitas.conFranjas(franjas, citas.listarAgendaConfirmada(profesionalId, desde, hasta));
    }

    /** Listado paginado de todas las citas (RA-04), de la mas reciente a la mas antigua. Solo ADMINISTRATIVO. */
    public PaginaCitasConFranja listarCitas(Actor actor, FiltroCitas filtro, int pagina, int tamano) {
        return transacciones.ejecutarSoloLectura(() -> {
            if (!actor.esAdministrativo()) {
                throw new AccesoProhibidoException("SOLO_ADMINISTRATIVO", "Solo el personal administrativo lista todas las citas");
            }
            ReglasCita.validarPaginacion(pagina, tamano);
            PaginaCitas p = citas.listar(filtro, pagina, tamano);
            return new PaginaCitasConFranja(AyudaCitas.conFranjas(franjas, p.contenido()), p.pagina(), p.tamano(), p.totalElementos(), p.totalPaginas());
        });
    }

    /** CU-24: el paciente y el profesional de la cita, y el personal administrativo. */
    public HistorialCita[] consultarHistorialCita(Actor actor, String citaId) {
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        AyudaCitas.exigirLectura(catalog, actor, cita);
        return historial.listarPorCita(citaId);
    }

    /** Conteo de citas por estado. Solo ADMINISTRATIVO. */
    public EstadisticasCitas consultarEstadisticasCitas(Actor actor) {
        return transacciones.ejecutarSoloLectura(() -> {
            if (!actor.esAdministrativo()) {
                throw new AccesoProhibidoException("SOLO_ADMINISTRATIVO", "Solo el personal administrativo consulta las estadisticas");
            }
            return citas.estadisticas();
        });
    }

    /** Consulta de una cita por otro servicio (clinical, CU-28): sin reglas de propiedad, solo ids y estado. Lanza 404 si no existe. */
    public Cita obtenerCitaServicio(String citaId) {
        return transacciones.ejecutarSoloLectura(() -> {
            return AyudaCitas.buscarCita(citas, citaId);
        });
    }

    /** CU-27: el profesional (id de catalog) ha tenido o tiene una cita con el paciente. */
    public boolean validarRelacion(String profesionalId, String pacienteId) {
        return transacciones.ejecutarSoloLectura(() -> {
            return citas.existeRelacion(profesionalId, pacienteId);
        });
    }
}
