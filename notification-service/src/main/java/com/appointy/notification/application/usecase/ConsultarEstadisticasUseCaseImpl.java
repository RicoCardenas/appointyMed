package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.usecase.ConsultarEstadisticasUseCase;

public class ConsultarEstadisticasUseCaseImpl implements ConsultarEstadisticasUseCase {

    private final NotificacionRepositoryGateway notificaciones;

    public ConsultarEstadisticasUseCaseImpl(NotificacionRepositoryGateway notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Override
    public Estadisticas ejecutar() {
        return notificaciones.estadisticas();
    }
}
