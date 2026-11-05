package com.appointy.catalog.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.HorarioAtencion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReglasHorarioTest {

    @Test
    void aceptaUnRangoQueLaDuracionDivideExactamente() {
        assertDoesNotThrow(() -> ReglasHorario.validar(1, 8 * 60, 12 * 60, 30));
        assertDoesNotThrow(() -> ReglasHorario.validar(7, 0, 1439, 1439));
    }

    @ParameterizedTest
    @CsvSource({"0,480,600,30,DIA_INVALIDO", "8,480,600,30,DIA_INVALIDO", "1,600,480,30,HORAS_INVALIDAS", "1,480,480,30,HORAS_INVALIDAS",
            "1,-1,60,30,HORAS_INVALIDAS", "1,480,1440,30,HORAS_INVALIDAS", "1,480,600,0,DURACION_INVALIDA", "1,480,600,45,DURACION_NO_DIVIDE_RANGO"})
    void rechazaHorariosIncoherentes(int dia, int inicio, int fin, int duracion, String codigo) {
        ValidacionException ex = assertThrows(ValidacionException.class, () -> ReglasHorario.validar(dia, inicio, fin, duracion));
        assertEquals(codigo, ex.codigo());
    }

    private static HorarioAtencion h(String id, int dia, int inicio, int fin, boolean activo) {
        return new HorarioAtencion(id, "p1", "c1", dia, inicio, fin, 30, activo);
    }

    @Test
    void detectaTraslapeElMismoDia() {
        HorarioAtencion[] existentes = {h("a", 1, 480, 600, true)};
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> ReglasHorario.validarSinTraslape(h("nuevo", 1, 570, 690, true), existentes));
        assertEquals("HORARIO_TRASLAPADO", ex.codigo());
    }

    @Test
    void tocarseEnElBordeNoEsTraslape() {
        HorarioAtencion[] existentes = {h("a", 1, 480, 600, true)};
        assertDoesNotThrow(() -> ReglasHorario.validarSinTraslape(h("nuevo", 1, 600, 720, true), existentes));
    }

    @Test
    void ignoraOtrosDiasLosInactivosYElMismoHorario() {
        HorarioAtencion[] existentes = {h("a", 2, 480, 600, true), h("b", 1, 480, 600, false), h("nuevo", 1, 480, 600, true)};
        assertDoesNotThrow(() -> ReglasHorario.validarSinTraslape(h("nuevo", 1, 500, 560, true), existentes));
    }
}
