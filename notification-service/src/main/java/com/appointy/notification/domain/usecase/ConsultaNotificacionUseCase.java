package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.rule.ReglasNotificacion;

/** listar notificaciones, listar notificaciones de cita, consultar estadisticas. */
public class ConsultaNotificacionUseCase {

    private final NotificacionRepositoryGateway notificaciones;

    public ConsultaNotificacionUseCase(NotificacionRepositoryGateway notificaciones) {
        this.notificaciones = notificaciones;
    }

    /** Listado paginado (RA-04), del mas reciente al mas antiguo. */
    public Pagina listarNotificaciones(FiltroNotificaciones filtro, int pagina, int tamano) {
        ReglasNotificacion.validarPaginacion(pagina, tamano);
        return notificaciones.listar(filtro, pagina, tamano);
    }

    public Notificacion[] listarNotificacionesDeCita(String citaId) {
        if (citaId == null || citaId.isBlank()) {
            throw new ValidacionException("CAMPO_OBLIGATORIO", "citaId es obligatorio");
        }
        return notificaciones.listarPorCita(citaId);
    }

    public Estadisticas consultarEstadisticas() {
        return notificaciones.estadisticas();
    }
}
