package com.appointy.clinicalhistory.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.clinicalhistory.domain.exception.ValidacionException;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EstadoCita;
import org.junit.jupiter.api.Test;

class ReglasHistoriaTest {

    @Test
    void soloSeAtiendeUnaCitaConfirmadaOCompletada() {
        assertTrue(ReglasHistoria.citaAtendible(EstadoCita.CONFIRMADA));
        assertTrue(ReglasHistoria.citaAtendible(EstadoCita.COMPLETADA));
        assertFalse(ReglasHistoria.citaAtendible(EstadoCita.CANCELADA));
        assertFalse(ReglasHistoria.citaAtendible(EstadoCita.NO_ASISTIDA));
    }

    @Test
    void motivoYDiagnosticoSonObligatoriosYNotasEIndicacionesNo() {
        assertDoesNotThrow(() -> ReglasHistoria.validarContenido(new ContenidoClinico("m", "d", null, null)));
        assertEquals("MOTIVO_OBLIGATORIO", assertThrows(ValidacionException.class,
                () -> ReglasHistoria.validarContenido(new ContenidoClinico("  ", "d", null, null))).codigo());
        assertEquals("DIAGNOSTICO_OBLIGATORIO", assertThrows(ValidacionException.class,
                () -> ReglasHistoria.validarContenido(new ContenidoClinico("m", null, null, null))).codigo());
    }

    @Test
    void rechazaTextosLargosSinRepetirElContenidoEnElMensaje() {
        String largo = "x".repeat(ReglasHistoria.MAXIMO_CARACTERES + 1);
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> ReglasHistoria.validarContenido(new ContenidoClinico("m", "d", largo, null)));
        assertEquals("TEXTO_DEMASIADO_LARGO", e.codigo());
        assertFalse(e.getMessage().contains("xxx"));
    }
}
