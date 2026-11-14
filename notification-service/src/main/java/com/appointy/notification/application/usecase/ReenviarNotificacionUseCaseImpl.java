package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.exception.NoEncontradoException;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.rule.ReglasNotificacion;
import com.appointy.notification.domain.usecase.ReenviarNotificacionUseCase;

public class ReenviarNotificacionUseCaseImpl implements ReenviarNotificacionUseCase {

    private final NotificacionRepositoryGateway notificaciones;
    private final Despachador despachador;

    public ReenviarNotificacionUseCaseImpl(NotificacionRepositoryGateway notificaciones, Despachador despachador) {
        this.notificaciones = notificaciones;
        this.despachador = despachador;
    }

    @Override
    public Notificacion ejecutar(String id) {
        Notificacion notificacion = notificaciones.buscarPorId(id);
        if (notificacion == null) {
            throw new NoEncontradoException("NOTIFICACION_NO_ENCONTRADA", "La notificacion no existe");
        }
        ReglasNotificacion.validarReenvio(notificacion);
        return despachador.despachar(notificacion);
    }
}
