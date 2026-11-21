package com.appointy.clinicalhistory.domain.rule;

/** Auxiliares para arreglos de texto (el dominio no usa colecciones del JDK). */
public final class Arreglos {

    private Arreglos() {}

    public static boolean contiene(String[] valores, String buscado) {
        for (String v : valores) {
            if (v.equals(buscado)) {
                return true;
            }
        }
        return false;
    }

    /** Quita repetidos conservando el orden de la primera aparicion. */
    public static String[] sinDuplicados(String[] valores) {
        String[] salida = new String[valores.length];
        int n = 0;
        for (String v : valores) {
            boolean repetido = false;
            for (int i = 0; i < n; i++) {
                if (salida[i].equals(v)) {
                    repetido = true;
                    break;
                }
            }
            if (!repetido) {
                salida[n++] = v;
            }
        }
        String[] recortado = new String[n];
        System.arraycopy(salida, 0, recortado, 0, n);
        return recortado;
    }
}
