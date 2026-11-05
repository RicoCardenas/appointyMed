package com.appointy.appointment.domain.port.out;

import com.appointy.appointment.domain.model.EventoCita;

/** Aviso a notification-service. Lo llama el oyente posterior al commit; su fallo nunca revierte la cita. */
public interface NotificacionClientPort {

    void notificar(EventoCita evento);
}
