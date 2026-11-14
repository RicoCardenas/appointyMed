package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.Notificacion;

public interface ListarNotificacionesDeCitaUseCase {

    Notificacion[] ejecutar(String citaId);
}
