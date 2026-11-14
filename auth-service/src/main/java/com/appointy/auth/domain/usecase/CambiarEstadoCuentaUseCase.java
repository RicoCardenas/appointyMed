package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.Usuario;

/** Activa o desactiva una cuenta. Idempotente: repetir la operacion no falla. */
public interface CambiarEstadoCuentaUseCase {

    Usuario ejecutar(Comando comando);

    record Comando(String actorId, String usuarioId, boolean activo) {}
}
