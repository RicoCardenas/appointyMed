package com.appointy.auth.domain.gateway;

import com.appointy.auth.domain.model.TokenRevocado;

public interface TokenRevocadoRepositoryGateway {

    void guardar(TokenRevocado token);

    boolean existePorJti(String jti);

    /** Elimina los revocados cuyo token ya vencio por si solo; devuelve cuantos borro. */
    int eliminarVencidosAntesDe(long limiteEnMilisegundos);
}
