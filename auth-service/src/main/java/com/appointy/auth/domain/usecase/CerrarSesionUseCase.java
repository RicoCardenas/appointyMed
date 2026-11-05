package com.appointy.auth.domain.usecase;

/** CU-03: revoca el token actual hasta su vencimiento natural. {@code expiraEn}: milisegundos desde 1970 en UTC. */
public interface CerrarSesionUseCase {

    void ejecutar(Comando comando);

    record Comando(String jti, long expiraEn) {}
}
