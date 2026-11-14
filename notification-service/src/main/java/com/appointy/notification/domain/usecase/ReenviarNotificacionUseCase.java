package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.Notificacion;

/** Reenvio manual de una notificacion PENDIENTE o FALLIDA por un administrativo. */
public interface ReenviarNotificacionUseCase {

    Notificacion ejecutar(String id);
}
