package com.appointy.auth.domain.port.in;

import java.time.Instant;

/** CU-03: revoca el token actual hasta su vencimiento natural. */
public interface CerrarSesionPort {

    void ejecutar(Comando comando);

    record Comando(String jti, Instant expiraEn) {}
}
