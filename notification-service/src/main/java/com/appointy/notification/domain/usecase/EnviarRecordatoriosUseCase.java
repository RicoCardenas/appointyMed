package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.model.ResultadoLote;

/** CU-34: envia los recordatorios que ya vencieron (24 h antes de la cita). */
public interface EnviarRecordatoriosUseCase {

    ResultadoLote ejecutar();
}
