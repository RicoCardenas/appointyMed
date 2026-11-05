package com.appointy.catalog.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.catalog.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;

class ReglasBloqueoTest {

    @Test
    void aceptaUnRangoDeUnDiaOMas() {
        assertDoesNotThrow(() -> ReglasBloqueo.validar("2026-07-01", "2026-07-01", "Vacaciones"));
        assertDoesNotThrow(() -> ReglasBloqueo.validar("2026-07-01", "2026-07-15", "Vacaciones"));
    }

    @Test
    void rechazaFinAnteriorAlInicioYMotivoVacio() {
        assertThrows(ValidacionException.class, () -> ReglasBloqueo.validar("2026-07-02", "2026-07-01", "x"));
        assertThrows(ValidacionException.class, () -> ReglasBloqueo.validar("2026-07-01", "2026-07-02", " "));
        assertThrows(ValidacionException.class, () -> ReglasBloqueo.validar(null, "2026-07-02", "x"));
    }
}
