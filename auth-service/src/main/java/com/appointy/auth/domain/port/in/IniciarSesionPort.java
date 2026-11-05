package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.Rol;
import java.time.Instant;
import java.util.UUID;

/** CU-02. */
public interface IniciarSesionPort {

    Resultado ejecutar(Comando comando);

    record Comando(String correo, String clave) {}

    record Resultado(String token, Instant expiraEn, UUID usuarioId, Rol rol) {}
}
