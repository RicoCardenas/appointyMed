package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.Rol;

/** CU-02. {@code expiraEn}: milisegundos desde 1970 en UTC. */
public interface IniciarSesionUseCase {

    Resultado ejecutar(Comando comando);

    record Comando(String correo, String clave) {}

    record Resultado(String token, long expiraEn, String usuarioId, Rol rol) {}
}
