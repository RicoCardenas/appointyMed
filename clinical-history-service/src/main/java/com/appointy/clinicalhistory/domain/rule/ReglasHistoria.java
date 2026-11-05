package com.appointy.clinicalhistory.domain.rule;

import com.appointy.clinicalhistory.domain.exception.ValidacionException;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EstadoCita;

/** Reglas de la historia clinica, sin estado. Los mensajes nunca repiten el contenido recibido. */
public final class ReglasHistoria {

    public static final int MAXIMO_CARACTERES = 5000;

    private ReglasHistoria() {}

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
