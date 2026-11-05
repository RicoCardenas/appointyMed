package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.EventoCita;

/** Aviso a notification-service. Lo llama el oyente posterior al commit; su fallo nunca revierte la cita. */
public interface NotificacionClientGateway {

    void notificar(EventoCita evento);
}
