package com.appointy.catalog.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.catalog.infrastructure.entry_points.dto.Horas;
import org.junit.jupiter.api.Test;

class HorasTest {

    @Test
    void convierteEntreTextoYMinutos() {
        assertEquals(510, Horas.aMinutos("08:30"));
        assertEquals(0, Horas.aMinutos("00:00"));
        assertEquals(1439, Horas.aMinutos("23:59"));
        assertEquals("08:30", Horas.aTexto(510));
        assertEquals("23:59", Horas.aTexto(1439));
    }

    @Test
    void elPatronAceptaSoloHorasValidas() {
        for (String ok : new String[] {"00:00", "09:05", "19:59", "23:59"}) {
            assertTrue(ok.matches(Horas.PATRON), ok);
        }
        for (String mal : new String[] {"24:00", "8:30", "12:60", "12-30", "", "1230"}) {
            assertFalse(mal.matches(Horas.PATRON), mal);
        }
    }
}
