package com.appointy.catalog.domain.rule;

import com.appointy.catalog.domain.exception.ValidacionException;

/** Los cuatro parametros del servicio (RF-14), con el rango de valores que acepta cada uno. */
public final class ReglasParametro {

    public static final String ANTICIPACION_MINIMA_HORAS = "anticipacion_minima_horas";
    public static final String LIMITE_CANCELACION_HORAS = "limite_cancelacion_horas";
    public static final String HORIZONTE_MAX_DIAS = "horizonte_max_dias";
    public static final String DURACION_FRANJA_MIN = "duracion_franja_min";

    private ReglasParametro() {}

    public static void validar(String clave, String valor) {
        if (ANTICIPACION_MINIMA_HORAS.equals(clave) || LIMITE_CANCELACION_HORAS.equals(clave)) {
            exigirEntre(clave, valor, 0, 720);
        } else if (HORIZONTE_MAX_DIAS.equals(clave)) {
            exigirEntre(clave, valor, 1, 365);
        } else if (DURACION_FRANJA_MIN.equals(clave)) {
            exigirEntre(clave, valor, 5, 240);
        } else {
            throw new ValidacionException("PARAMETRO_DESCONOCIDO", "Parametro no reconocido: " + clave);
        }
    }

    public static int comoEntero(String valor) {
        return Integer.parseInt(valor.trim());
    }

    private static void exigirEntre(String clave, String valor, int minimo, int maximo) {
        int numero;
        try {
            numero = Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("PARAMETRO_INVALIDO", clave + " debe ser un numero entero");
        }
        if (numero < minimo || numero > maximo) {
            throw new ValidacionException("PARAMETRO_FUERA_DE_RANGO", clave + " debe estar entre " + minimo + " y " + maximo);
        }
    }
}
