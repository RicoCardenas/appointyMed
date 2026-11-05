package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.EventoCita;

/** Registra el evento dentro de la transaccion; se entrega solo si la transaccion confirma (AFTER_COMMIT). */
public interface PublicadorEventosGateway {

    void publicar(EventoCita evento);
}
