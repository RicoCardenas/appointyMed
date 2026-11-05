package com.appointy.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class GeneradorFranjasTest {

    private static HorarioAtencion horario(int dia, int inicio, int fin, int duracion) {
        return new HorarioAtencion("h1", "p1", "c1", dia, inicio, fin, duracion, true);
    }

    @Test
    void generaLasFranjasDeCadaLunesDelRango() {
        // 2026-06-01 es lunes; el rango incluye los lunes 1, 8 y 15
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario(1, 8 * 60, 10 * 60, 30), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15));

        assertEquals(12, franjas.length);
        assertEquals(new FranjaSolicitada("2026-06-01", 480, 510), franjas[0]);
        assertEquals(new FranjaSolicitada("2026-06-01", 570, 600), franjas[3]);
        assertEquals(new FranjaSolicitada("2026-06-08", 480, 510), franjas[4]);
        assertEquals(new FranjaSolicitada("2026-06-15", 570, 600), franjas[11]);
    }

    @Test
    void empiezaEnElPrimerDiaQueCoincideSiElRangoNoEmpiezaEseDia() {
        // 2026-06-03 es miercoles; el primer viernes (5) es el 2026-06-05
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario(5, 600, 660, 60), LocalDate.of(2026, 6, 3), LocalDate.of(2026, 6, 12));

        assertEquals(2, franjas.length);
        assertEquals("2026-06-05", franjas[0].fecha());
        assertEquals("2026-06-12", franjas[1].fecha());
    }

    @Test
    void sinDiaCoincidenteNoHayFranjas() {
        assertEquals(0, GeneradorFranjas.generar(horario(7, 480, 600, 30), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 6)).length);
    }

    @Test
    void ningunaFranjaSePasaDelFinDelHorario() {
        FranjaSolicitada[] franjas = GeneradorFranjas.generar(horario(1, 480, 600, 40), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1));

        assertEquals(3, franjas.length);
        assertEquals(600, franjas[2].horaFin());
    }
}
