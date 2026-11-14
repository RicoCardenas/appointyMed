package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.rule.ReglasNotificacion;
import com.appointy.notification.domain.usecase.ListarNotificacionesUseCase;

public class ListarNotificacionesUseCaseImpl implements ListarNotificacionesUseCase {

    private final NotificacionRepositoryGateway notificaciones;

    public ListarNotificacionesUseCaseImpl(NotificacionRepositoryGateway notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Override
    public Pagina ejecutar(FiltroNotificaciones filtro, int pagina, int tamano) {
        ReglasNotificacion.validarPaginacion(pagina, tamano);
        return notificaciones.listar(filtro, pagina, tamano);
    }
}
