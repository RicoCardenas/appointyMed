package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.SolicitudCita;

/** CU-33: registra y envia el correo de una cita confirmada, reprogramada o cancelada. Idempotente. */
public interface NotificarCitaUseCase {

    Notificacion ejecutar(SolicitudCita solicitud);
}
