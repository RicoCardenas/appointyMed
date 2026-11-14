package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.ResultadoLote;

/** Procesa un lote: una notificacion que falla no detiene a las demas. */
final class Lotes {

    private Lotes() {}

    static ResultadoLote procesar(Notificacion[] lote, Despachador despachador) {
        int enviadas = 0;
        int fallidas = 0;
        for (Notificacion n : lote) {
            if (despachador.despachar(n).estado() == EstadoNotificacion.ENVIADA) {
                enviadas++;
            } else {
                fallidas++;
            }
        }
        return new ResultadoLote(lote.length, enviadas, fallidas);
    }
}
