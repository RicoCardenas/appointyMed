package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.TokenRevocado;

public interface TokenRevocadoRepositoryPort {

    void guardar(TokenRevocado token);

    boolean existePorJti(String jti);

    /** Elimina los revocados cuyo token ya vencio por si solo; devuelve cuantos borro. */
    int eliminarVencidosAntesDe(long limiteEnMilisegundos);
}
