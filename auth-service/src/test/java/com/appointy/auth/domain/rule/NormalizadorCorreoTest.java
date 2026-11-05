package com.appointy.auth.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.auth.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NormalizadorCorreoTest {

    @Test
    void recortaYPasaAMinusculas() {
        assertEquals("ana@correo.com", NormalizadorCorreo.normalizar("  Ana@Correo.COM "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "sin-arroba", "@dominio.com", "usuario@", "a@@b.com"})
    void rechazaCorreosMalFormados(String correo) {
        assertThrows(ValidacionException.class, () -> NormalizadorCorreo.normalizar(correo));
    }

    @Test
    void comparableNuncaLanzaYAceptaNulo() {
        assertEquals("", NormalizadorCorreo.comparable(null));
        assertEquals("x", NormalizadorCorreo.comparable(" X "));
    }
}
