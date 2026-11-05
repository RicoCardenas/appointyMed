package com.appointy.auth.domain.port.in;

/** CU-03: revoca el token actual hasta su vencimiento natural. {@code expiraEn}: milisegundos desde 1970 en UTC. */
public interface CerrarSesionPort {

    void ejecutar(Comando comando);

    record Comando(String jti, long expiraEn) {}
}
