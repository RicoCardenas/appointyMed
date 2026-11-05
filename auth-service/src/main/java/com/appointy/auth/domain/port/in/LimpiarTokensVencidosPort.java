package com.appointy.auth.domain.port.in;

/** RF-03: elimina de la lista de revocados los tokens que ya expiraron por si solos. */
public interface LimpiarTokensVencidosPort {

    int ejecutar();
}
