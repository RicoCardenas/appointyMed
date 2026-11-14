package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.usecase.ReintentarFallidasUseCase;

public class ReintentarFallidasUseCaseImpl implements ReintentarFallidasUseCase {

    private final NotificacionRepositoryGateway notificaciones;
    private final Despachador despachador;
    private final int maxIntentos;
    private final int lote;

    public ReintentarFallidasUseCaseImpl(NotificacionRepositoryGateway notificaciones, Despachador despachador, int maxIntentos, int lote) {
        this.notificaciones = notificaciones;
        this.despachador = despachador;
        this.maxIntentos = maxIntentos;
        this.lote = lote;
    }

    @Override
    public ResultadoLote ejecutar() {
        return Lotes.procesar(notificaciones.fallidasReintentables(maxIntentos, lote), despachador);
    }
}
