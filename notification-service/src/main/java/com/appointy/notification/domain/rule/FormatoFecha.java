package com.appointy.notification.domain.rule;

/**
 * Formato legible de fechas y horas para los correos. Sin imports: el dia de la semana se calcula a mano
 * (algoritmo de Sakamoto) porque el dominio no usa java.time.
 */
public final class FormatoFecha {

    private static final String[] DIAS = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
    private static final String[] DIAS_CORTOS = {"dom", "lun", "mar", "mié", "jue", "vie", "sáb"};
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre",
        "octubre", "noviembre", "diciembre"};
    private static final int[] DESFASE = {0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4};

    private FormatoFecha() {}

    /** Ejemplo: "miércoles 2 de diciembre de 2026". Entrada validada como yyyy-MM-dd. */
    public static String fechaLarga(String iso) {
        return DIAS[diaSemana(iso)] + " " + dia(iso) + " de " + MESES[mes(iso) - 1] + " de " + anio(iso);
    }

    /** Ejemplo: "mié 2 dic". */
    public static String fechaCorta(String iso) {
        return DIAS_CORTOS[diaSemana(iso)] + " " + dia(iso) + " " + MESES[mes(iso) - 1].substring(0, 3);
    }

    /** Ejemplo: "08:30" -> "8:30 a. m."; "17:05" -> "5:05 p. m."; "00:15" -> "12:15 a. m.". Entrada validada como HH:mm. */
    public static String hora(String hhmm) {
        int h = Integer.parseInt(hhmm.substring(0, 2));
        String minutos = hhmm.substring(3, 5);
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return h12 + ":" + minutos + (h < 12 ? " a. m." : " p. m.");
    }

    /** Los primeros 8 caracteres del id, en mayuscula: suficiente para citarlo al llamar o escribir a la clinica. */
    public static String codigoCorto(String citaId) {
        return (citaId.length() > 8 ? citaId.substring(0, 8) : citaId).toUpperCase();
    }

    private static int anio(String iso) {
        return Integer.parseInt(iso.substring(0, 4));
    }

    private static int mes(String iso) {
        return Integer.parseInt(iso.substring(5, 7));
    }

    private static int dia(String iso) {
        return Integer.parseInt(iso.substring(8, 10));
    }

    /** 0 = domingo ... 6 = sabado. */
    private static int diaSemana(String iso) {
        int y = anio(iso);
        int m = mes(iso);
        if (m < 3) {
            y -= 1;
        }
        return (y + y / 4 - y / 100 + y / 400 + DESFASE[m - 1] + dia(iso)) % 7;
    }
}
