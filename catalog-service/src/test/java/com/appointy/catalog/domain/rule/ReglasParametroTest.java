package com.appointy.catalog.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.catalog.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReglasParametroTest {

    @ParameterizedTest
    @CsvSource({"anticipacion_minima_horas,0", "anticipacion_minima_horas,720", "limite_cancelacion_horas,24", "horizonte_max_dias,1",
            "horizonte_max_dias,365", "duracion_franja_min,5", "duracion_franja_min,240"})
    void aceptaValoresDentroDelRango(String clave, String valor) {
        assertDoesNotThrow(() -> ReglasParametro.validar(clave, valor));
    }

    @ParameterizedTest
    @CsvSource({"horizonte_max_dias,0,PARAMETRO_FUERA_DE_RANGO", "horizonte_max_dias,366,PARAMETRO_FUERA_DE_RANGO",
            "duracion_franja_min,4,PARAMETRO_FUERA_DE_RANGO", "anticipacion_minima_horas,-1,PARAMETRO_FUERA_DE_RANGO",
            "limite_cancelacion_horas,abc,PARAMETRO_INVALIDO", "duracion_franja_min,'',PARAMETRO_INVALIDO",
            "no_existe,5,PARAMETRO_DESCONOCIDO"})
    void rechazaValoresInvalidosOClavesDesconocidas(String clave, String valor, String codigo) {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> ReglasParametro.validar(clave, valor));
        assertEquals(codigo, ex.codigo());
    }

    @Test
    void rechazaUnValorNulo() {
        assertThrows(ValidacionException.class, () -> ReglasParametro.validar("horizonte_max_dias", null));
    }
}
