package com.appointy.auth.domain.rule;

import com.appointy.auth.domain.exception.ValidacionException;
import java.nio.charset.StandardCharsets;

/** Politica de claves: de 8 a 72 bytes (limite de BCrypt), con al menos una letra y un digito. */
public final class PoliticaClave {

    private static final int MINIMO = 8;
    private static final int MAXIMO_BYTES = 72;

    private PoliticaClave() {}

    public static void validar(String clave) {
        if (clave == null || clave.length() < MINIMO) {
            throw debil("La clave debe tener al menos " + MINIMO + " caracteres");
        }
        if (clave.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            throw debil("La clave no puede superar " + MAXIMO_BYTES + " bytes");
        }
        if (clave.chars().noneMatch(Character::isLetter) || clave.chars().noneMatch(Character::isDigit)) {
            throw debil("La clave debe incluir al menos una letra y un digito");
        }
    }

    private static ValidacionException debil(String mensaje) {
        return new ValidacionException("CLAVE_DEBIL", mensaje);
    }
}
