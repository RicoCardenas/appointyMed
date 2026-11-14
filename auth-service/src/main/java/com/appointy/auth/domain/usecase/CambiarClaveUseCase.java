package com.appointy.auth.domain.usecase;

/** El usuario autenticado cambia su propia clave. Los tokens ya emitidos siguen validos hasta expirar (decision 9). */
public interface CambiarClaveUseCase {

    void ejecutar(Comando comando);

    record Comando(String usuarioId, String claveActual, String claveNueva) {}
}
