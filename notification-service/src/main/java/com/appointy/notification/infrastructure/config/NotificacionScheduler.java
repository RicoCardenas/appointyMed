package com.appointy.notification.infrastructure.config;

import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.usecase.EnviarRecordatoriosUseCase;
import com.appointy.notification.domain.usecase.ReintentarFallidasUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** CU-34 y CU-35. Pensado para una sola instancia; con varias habria que bloquear el lote (SKIP LOCKED o ShedLock). */
@Component
public class NotificacionScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionScheduler.class);

    private final EnviarRecordatoriosUseCase recordatorios;
    private final ReintentarFallidasUseCase reintentos;

    public NotificacionScheduler(EnviarRecordatoriosUseCase recordatorios, ReintentarFallidasUseCase reintentos) {
        this.recordatorios = recordatorios;
        this.reintentos = reintentos;
    }

    @Scheduled(fixedDelayString = "${app.jobs.recordatorios-ms:60000}", initialDelayString = "${app.jobs.recordatorios-ms:60000}")
    void enviarRecordatorios() {
        try {
            ResultadoLote r = recordatorios.ejecutar();
            if (r.procesadas() > 0) LOG.info("Recordatorios: procesadas={}, enviadas={}, fallidas={}", r.procesadas(), r.enviadas(), r.fallidas());
        } catch (RuntimeException e) {
            LOG.error("Fallo el job de recordatorios ({})", e.getClass().getName());
        }
    }

    @Scheduled(fixedDelayString = "${app.jobs.reintentos-ms:300000}", initialDelayString = "${app.jobs.reintentos-ms:300000}")
    void reintentarFallidas() {
        try {
            ResultadoLote r = reintentos.ejecutar();
            if (r.procesadas() > 0) LOG.info("Reintentos: procesadas={}, enviadas={}, fallidas={}", r.procesadas(), r.enviadas(), r.fallidas());
        } catch (RuntimeException e) {
            LOG.error("Fallo el job de reintentos ({})", e.getClass().getName());
        }
    }
}
