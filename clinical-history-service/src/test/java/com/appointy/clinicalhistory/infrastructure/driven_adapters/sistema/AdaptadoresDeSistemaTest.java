package com.appointy.clinicalhistory.infrastructure.driven_adapters.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class AdaptadoresDeSistemaTest {

    @Test
    void elRelojEntregaMillisYFechaIso() {
        // Arrange
        RelojAdapter reloj = new RelojAdapter(Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), ZoneOffset.UTC));
        // Act + Assert
        assertEquals(Instant.parse("2026-11-21T03:00:00Z").toEpochMilli(), reloj.ahora());
        assertEquals("2026-11-21", reloj.hoy());
    }

    @Test
    void losIdsSonUuidDistintos() {
        UuidGeneradorIdAdapter ids = new UuidGeneradorIdAdapter();
        String a = ids.nuevoId();
        assertEquals(36, a.length());
        assertNotEquals(a, ids.nuevoId());
    }

    @Test
    void elRegistroNoLanzaExcepciones() {
        Slf4jRegistroAdapter registro = new Slf4jRegistroAdapter();
        registro.info("mensaje");
        registro.error("error");
    }

    @Test
    void laTransaccionConfirmaSiTodoSaleBienYRevierteSiFalla() {
        // Arrange
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        TransactionStatus estado = mock(TransactionStatus.class);
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(estado);
        SpringTransactionAdapter tx = new SpringTransactionAdapter(manager);
        // Act
        String normal = tx.ejecutar(() -> "ok");
        boolean lectura = tx.ejecutarSoloLectura(() -> true);
        assertThrows(IllegalStateException.class, () -> tx.ejecutar(() -> {
            throw new IllegalStateException("falla");
        }));
        // Assert
        assertEquals("ok", normal);
        assertFalse(!lectura);
        verify(manager, org.mockito.Mockito.times(1)).rollback(estado);
        verify(manager, org.mockito.Mockito.times(2)).commit(estado);
    }
}
