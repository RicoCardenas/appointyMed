package com.appointy.appointment.infrastructure.driven_adapters.events;

import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.port.out.NotificacionClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * CU-33 paso 8: solo se ejecuta si la transaccion confirmo (AFTER_COMMIT) y en otro hilo (@Async). Un fallo se registra y
 * muere aqui: la cita ya esta guardada y no se revierte (RNF-07).
 */
@Component
public class NotificacionEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionEventListener.class);

    private final NotificacionClientPort cliente;

    public NotificacionEventListener(NotificacionClientPort cliente) {
        this.cliente = cliente;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmar(EventoCita evento) {
        try {
            cliente.notificar(evento);
        } catch (RuntimeException e) {
            LOG.error("No se pudo notificar el evento {} de la cita {}: {}", evento.tipo(), evento.citaId(), e.getClass().getSimpleName());
        }
    }
}
