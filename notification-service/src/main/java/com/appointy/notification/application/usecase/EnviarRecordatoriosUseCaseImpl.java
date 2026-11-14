package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.usecase.EnviarRecordatoriosUseCase;
import java.time.Clock;

public class EnviarRecordatoriosUseCaseImpl implements EnviarRecordatoriosUseCase {

    private final NotificacionRepositoryGateway notificaciones;
    private final Despachador despachador;
    private final Clock reloj;
    private final int lote;

    public EnviarRecordatoriosUseCaseImpl(NotificacionRepositoryGateway notificaciones, Despachador despachador, Clock reloj, int lote) {
        this.notificaciones = notificaciones;
        this.despachador = despachador;
        this.reloj = reloj;
        this.lote = lote;
    }

    @Override
    public ResultadoLote ejecutar() {
        return Lotes.procesar(notificaciones.recordatoriosVencidos(reloj.millis(), lote), despachador);
    }
}
