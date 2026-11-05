package com.appointy.auth.domain.usecase;

/** RF-03: elimina de la lista de revocados los tokens que ya expiraron por si solos. */
public interface LimpiarTokensVencidosUseCase {

    int ejecutar();
}
