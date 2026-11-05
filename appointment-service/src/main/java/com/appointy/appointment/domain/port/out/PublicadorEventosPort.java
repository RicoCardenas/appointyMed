package com.appointy.appointment.domain.port.out;

import com.appointy.appointment.domain.model.EventoCita;

/** Registra el evento dentro de la transaccion; se entrega solo si la transaccion confirma (AFTER_COMMIT). */
public interface PublicadorEventosPort {

    void publicar(EventoCita evento);
}
