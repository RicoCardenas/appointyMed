package com.appointy.auth.domain.rule;

import com.appointy.auth.domain.exception.ValidacionException;

/** Politica de claves: de 8 a 72 bytes UTF-8 (limite de BCrypt), con al menos una letra y un digito. */
public final class PoliticaClave {

    private static final int MINIMO = 8;
    private static final int MAXIMO_BYTES = 72;

    private PoliticaClave() {}

    public static void validar(String clave) {
        if (clave == null || clave.length() < MINIMO) {
            throw debil("La clave debe tener al menos " + MINIMO + " caracteres");
        }
        if (bytesUtf8(clave) > MAXIMO_BYTES) {
            throw debil("La clave no puede superar " + MAXIMO_BYTES + " bytes");
        }
        boolean hayLetra = false;
        boolean hayDigito = false;
        for (int i = 0; i < clave.length(); i++) {
            char c = clave.charAt(i);
            if (Character.isLetter(c)) {
                hayLetra = true;
            } else if (Character.isDigit(c)) {
                hayDigito = true;
            }
        }
        if (!hayLetra || !hayDigito) {
            throw debil("La clave debe incluir al menos una letra y un digito");
        }
    }

    /** Tamano UTF-8 calculado a mano para no depender de java.nio en el dominio. */
    static int bytesUtf8(String texto) {
        int bytes = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c < 0x80) {
                bytes += 1;
            } else if (c < 0x800) {
                bytes += 2;
            } else if (Character.isHighSurrogate(c)) {
                bytes += 4;
                i++;
            } else {
                bytes += 3;
            }
        }
        return bytes;
    }

    private static ValidacionException debil(String mensaje) {
        return new ValidacionException("CLAVE_DEBIL", mensaje);
    }
}
