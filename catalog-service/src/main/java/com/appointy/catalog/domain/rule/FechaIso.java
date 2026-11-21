package com.appointy.catalog.domain.rule;

/** Fechas de calendario como texto ISO yyyy-MM-dd, con aritmetica propia para no depender de librerias. */
public final class FechaIso {

    private FechaIso() {}

    public static boolean esValida(String iso) {
        if (iso == null || iso.length() != 10 || iso.charAt(4) != '-' || iso.charAt(7) != '-') {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (i != 4 && i != 7 && (iso.charAt(i) < '0' || iso.charAt(i) > '9')) {
                return false;
            }
        }
        int mes = Integer.parseInt(iso.substring(5, 7));
        int dia = Integer.parseInt(iso.substring(8, 10));
        return mes >= 1 && mes <= 12 && dia >= 1 && dia <= diasEnMes(Integer.parseInt(iso.substring(0, 4)), mes);
    }

    public static int diasEnMes(int anio, int mes) {
        if (mes == 2) {
            return bisiesto(anio) ? 29 : 28;
        }
        return (mes == 4 || mes == 6 || mes == 9 || mes == 11) ? 30 : 31;
    }

    private static boolean bisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0;
    }

    /** Dias transcurridos desde 1970-01-01 (negativo antes). */
    public static long diasDesdeEpoca(String iso) {
        long y = Integer.parseInt(iso.substring(0, 4));
        int m = Integer.parseInt(iso.substring(5, 7));
        int d = Integer.parseInt(iso.substring(8, 10));
        if (m <= 2) {
            y -= 1;
        }
        long era = Math.floorDiv(y, 400L);
        long yoe = y - era * 400;
        long doy = (153L * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
        return era * 146097 + doe - 719468;
    }

    public static String deDiasDesdeEpoca(long dias) {
        long z = dias + 719468;
        long era = Math.floorDiv(z, 146097L);
        long doe = z - era * 146097;
        long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
        long y = yoe + era * 400;
        long doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
        long mp = (5 * doy + 2) / 153;
        long d = doy - (153 * mp + 2) / 5 + 1;
        long m = mp < 10 ? mp + 3 : mp - 9;
        if (m <= 2) {
            y += 1;
        }
        return String.format("%04d-%02d-%02d", y, m, d);
    }

    public static String sumarDias(String iso, int dias) {
        return deDiasDesdeEpoca(diasDesdeEpoca(iso) + dias);
    }

    /** 1 = lunes ... 7 = domingo. */
    public static int diaDeSemana(String iso) {
        return (int) Math.floorMod(diasDesdeEpoca(iso) + 3, 7L) + 1;
    }
}
