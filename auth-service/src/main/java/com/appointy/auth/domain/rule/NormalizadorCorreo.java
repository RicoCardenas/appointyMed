package com.appointy.auth.domain.rule;

import com.appointy.auth.domain.exception.ValidacionException;
import java.util.Locale;

/** El correo se guarda siempre sin espacios y en minusculas; asi la unicidad no depende del uso de mayusculas. */
public final class NormalizadorCorreo {

    private static final int MAXIMO = 150;

    private NormalizadorCorreo() {}

    /** Para altas y cambios: rechaza lo que no parezca un correo. */
    public static String normalizar(String correo) {
        String limpio = comparable(correo);
        int arroba = limpio.indexOf('@');
        if (arroba < 1 || arroba != limpio.lastIndexOf('@') || arroba == limpio.length() - 1 || limpio.length() > MAXIMO) {
            throw new ValidacionException("CORREO_INVALIDO", "El correo no es valido");
        }
        return limpio;
    }

    /** Para busquedas (inicio de sesion): no rechaza nada, solo iguala la forma. */
    public static String comparable(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }
}
