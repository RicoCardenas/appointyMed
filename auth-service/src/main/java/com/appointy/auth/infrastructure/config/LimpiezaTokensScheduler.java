package com.appointy.auth.infrastructure.config;

import com.appointy.auth.domain.usecase.SesionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimpiezaTokensScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(LimpiezaTokensScheduler.class);

    private final SesionUseCase sesiones;

    public LimpiezaTokensScheduler(SesionUseCase sesiones) {
        this.sesiones = sesiones;
    }

    @Scheduled(fixedDelayString = "${app.tokens.limpieza-ms:3600000}", initialDelayString = "${app.tokens.limpieza-inicial-ms:60000}")
    public void limpiar() {
        int eliminados = sesiones.limpiarTokensVencidos();
        if (eliminados > 0) {
            LOG.info("Tokens revocados vencidos eliminados: {}", eliminados);
        }
    }
}
