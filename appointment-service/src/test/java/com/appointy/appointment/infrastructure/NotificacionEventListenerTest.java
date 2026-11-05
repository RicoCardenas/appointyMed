package com.appointy.appointment.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.appointy.appointment.domain.exception.ServicioNoDisponibleException;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.port.out.NotificacionClientPort;
import com.appointy.appointment.infrastructure.driven_adapters.events.NotificacionEventListener;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificacionEventListenerTest {

    static final EventoCita EVENTO = new EventoCita("CONFIRMADA", "c", "p", "pr", "ce", "2030-06-11", 480, "p");

    @Test
    void esAsincronoYSoloSeEjecutaTrasElCommit() throws NoSuchMethodException {
        Method m = NotificacionEventListener.class.getMethod("alConfirmar", EventoCita.class);
        assertNotNull(m.getAnnotation(Async.class));
        assertEquals(TransactionPhase.AFTER_COMMIT, m.getAnnotation(TransactionalEventListener.class).phase());
    }

    @Test
    void siNotificationFallaElErrorNoSePropaga() {
        NotificacionClientPort cliente = mock(NotificacionClientPort.class);
        doThrow(new ServicioNoDisponibleException("NOTIFICATION_NO_DISPONIBLE", "caido")).when(cliente).notificar(any());

        assertDoesNotThrow(() -> new NotificacionEventListener(cliente).alConfirmar(EVENTO));

        verify(cliente).notificar(EVENTO);
    }
}
