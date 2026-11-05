package com.appointy.auth.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.auth.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PoliticaClaveTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "corta1", "sinnumeros", "12345678", "a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1a1"})
    void rechazaClavesQueIncumplenLaPolitica(String clave) {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> PoliticaClave.validar(clave));
        assertEquals("CLAVE_DEBIL", ex.codigo());
    }

    @Test
    void aceptaClaveConLetraYDigito() {
        assertDoesNotThrow(() -> PoliticaClave.validar("Clave1234"));
    }
}
