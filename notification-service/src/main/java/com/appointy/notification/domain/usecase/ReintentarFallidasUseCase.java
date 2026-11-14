package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.ResultadoLote;

/** CU-35: reintenta las notificaciones fallidas que no llegaron al maximo de intentos. */
public interface ReintentarFallidasUseCase {

    ResultadoLote ejecutar();
}
