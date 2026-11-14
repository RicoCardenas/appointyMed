package com.appointy.clinicalhistory.domain.rule;

import com.appointy.clinicalhistory.domain.exception.ValidacionException;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.EstadoCita;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;

/** Reglas de la historia clinica, sin estado. Los mensajes nunca repiten el contenido recibido. */
public final class ReglasHistoria {

    public static final int MAXIMO_CARACTERES = 5000;

    public static final int TAMANO_MAXIMO_PAGINA = 100;
    public static final int MINIMO_TERMINO = 2;
    public static final int MAXIMO_TERMINO = 80;
    public static final int MAXIMO_DIAGNOSTICOS = 15;

    private ReglasHistoria() {}

    public static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ValidacionException("PAGINA_INVALIDA", "La pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new ValidacionException("TAMANO_INVALIDO", "El tamano debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA);
        }
    }

    /** El termino viaja a una API externa: se limita su largo y solo se aceptan letras, digitos, espacios y puntuacion simple. */
    public static String validarTermino(String termino) {
        String t = termino == null ? "" : termino.strip();
        if (t.length() < MINIMO_TERMINO || t.length() > MAXIMO_TERMINO) {
            throw new ValidacionException("TERMINO_INVALIDO", "El termino debe tener entre " + MINIMO_TERMINO + " y " + MAXIMO_TERMINO + " caracteres");
        }
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (!Character.isLetterOrDigit(c) && " .-,".indexOf(c) < 0) {
                throw new ValidacionException("TERMINO_INVALIDO", "El termino solo admite letras, digitos, espacios, punto, guion y coma");
            }
        }
        return t;
    }

    /** Las entradas llegan por fecha ascendente. */
    public static ResumenHistoria resumir(EntradaClinica[] entradas) {
        int atenciones = 0;
        int correcciones = 0;
        for (EntradaClinica e : entradas) {
            if (e.corrigeAId() == null) {
                atenciones++;
            } else {
                correcciones++;
            }
        }
        EntradaClinica ultima = entradas.length == 0 ? null : entradas[entradas.length - 1];
        return new ResumenHistoria(atenciones, correcciones, ultima == null ? 0 : ultima.creadoEn(), ultima == null ? null : ultima.diagnostico());
    }

    /** RN-17: solo se atiende una cita CONFIRMADA o COMPLETADA. */
    public static boolean citaAtendible(EstadoCita estado) {
        return estado == EstadoCita.CONFIRMADA || estado == EstadoCita.COMPLETADA;
    }

    /** Motivo y diagnostico son obligatorios; notas e indicaciones son opcionales. Ninguno supera {@value #MAXIMO_CARACTERES}. */
    public static void validarContenido(ContenidoClinico c) {
        if (c == null || vacio(c.motivo())) {
            throw new ValidacionException("MOTIVO_OBLIGATORIO", "El motivo de la atencion es obligatorio");
        }
        if (vacio(c.diagnostico())) {
            throw new ValidacionException("DIAGNOSTICO_OBLIGATORIO", "El diagnostico es obligatorio");
        }
        if (largo(c.motivo()) || largo(c.diagnostico()) || largo(c.notas()) || largo(c.indicaciones())) {
            throw new ValidacionException("TEXTO_DEMASIADO_LARGO", "Cada texto admite como maximo " + MAXIMO_CARACTERES + " caracteres");
        }
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static boolean largo(String texto) {
        return texto != null && texto.length() > MAXIMO_CARACTERES;
    }
}
