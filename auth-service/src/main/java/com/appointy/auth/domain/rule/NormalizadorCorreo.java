package com.appointy.auth.domain.rule;

import com.appointy.auth.domain.exception.ValidacionException;

/**
 * El correo se guarda sin espacios y en minusculas ASCII; asi la unicidad no depende del uso de mayusculas
 * ni de la configuracion regional. Por eso solo se admiten caracteres ASCII visibles.
 */
public final class NormalizadorCorreo {

    private static final int MAXIMO = 150;

    private NormalizadorCorreo() {}

    /** Para altas y cambios: rechaza lo que no parezca un correo. */
    public static String normalizar(String correo) {
        String limpio = comparable(correo);
        int arroba = limpio.indexOf('@');
        if (arroba < 1 || arroba != limpio.lastIndexOf('@') || arroba == limpio.length() - 1
                || limpio.length() > MAXIMO || !soloAsciiVisible(limpio)) {
            throw new ValidacionException("CORREO_INVALIDO", "El correo no es valido");
        }
        return limpio;
    }

    /** Para busquedas (inicio de sesion): no rechaza nada, solo iguala la forma. */
    public static String comparable(String correo) {
        if (correo == null) {
            return "";
        }
        char[] letras = correo.trim().toCharArray();
        for (int i = 0; i < letras.length; i++) {
            if (letras[i] >= 'A' && letras[i] <= 'Z') {
                letras[i] = (char) (letras[i] + ('a' - 'A'));
            }
        }
        return new String(letras);
    }

    private static boolean soloAsciiVisible(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c < 33 || c > 126) {
                return false;
            }
        }
        return true;
    }
}
