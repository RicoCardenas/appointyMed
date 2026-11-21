package com.appointy.clinicalhistory.domain.rule;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArreglosTest {

    @Test
    void quitaRepetidosConservandoElOrden() {
        // Arrange
        String[] entrada = {"b", "a", "b", "c", "a"};
        // Act
        String[] salida = Arreglos.sinDuplicados(entrada);
        // Assert
        assertArrayEquals(new String[] {"b", "a", "c"}, salida);
        assertArrayEquals(new String[0], Arreglos.sinDuplicados(new String[0]));
    }

    @Test
    void buscaElementos() {
        assertTrue(Arreglos.contiene(new String[] {"x", "y"}, "y"));
        assertFalse(Arreglos.contiene(new String[] {"x", "y"}, "z"));
    }
}
