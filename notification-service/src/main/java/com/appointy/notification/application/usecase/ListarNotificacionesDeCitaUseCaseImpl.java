package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.usecase.ListarNotificacionesDeCitaUseCase;

public class ListarNotificacionesDeCitaUseCaseImpl implements ListarNotificacionesDeCitaUseCase {

    private final NotificacionRepositoryGateway notificaciones;

    public ListarNotificacionesDeCitaUseCaseImpl(NotificacionRepositoryGateway notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Override
    public Notificacion[] ejecutar(String citaId) {
        if (citaId == null || citaId.isBlank()) {
            throw new ValidacionException("CAMPO_OBLIGATORIO", "citaId es obligatorio");
        }
        return notificaciones.listarPorCita(citaId);
    }
}
