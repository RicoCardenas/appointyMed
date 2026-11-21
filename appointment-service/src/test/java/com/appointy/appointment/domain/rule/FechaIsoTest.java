package com.appointy.appointment.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FechaIsoTest {

    @Test
    void validaFormatoYCalendario() {
        // Arrange: fechas validas e invalidas
        // Act + Assert
        assertTrue(FechaIso.esValida("2024-02-29"));
        assertFalse(FechaIso.esValida("2023-02-29"));
        assertFalse(FechaIso.esValida("1900-02-29"));
        assertTrue(FechaIso.esValida("2000-02-29"));
        assertFalse(FechaIso.esValida("2026-13-01"));
        assertFalse(FechaIso.esValida("2026-04-31"));
        assertFalse(FechaIso.esValida("2026-1-01"));
        assertFalse(FechaIso.esValida("2026/01/01"));
        assertFalse(FechaIso.esValida("20a6-01-01"));
        assertFalse(FechaIso.esValida(null));
    }

    @Test
    void convierteDiasDesdeLaEpocaEnAmbosSentidos() {
        // Arrange
        String fecha = "2026-11-21";
        // Act
        long dias = FechaIso.diasDesdeEpoca(fecha);
        // Assert
        assertEquals(0, FechaIso.diasDesdeEpoca("1970-01-01"));
        assertEquals(fecha, FechaIso.deDiasDesdeEpoca(dias));
        assertEquals("1969-12-31", FechaIso.deDiasDesdeEpoca(-1));
        assertEquals("2024-03-01", FechaIso.sumarDias("2024-02-28", 2));
        assertEquals("2025-12-31", FechaIso.sumarDias("2026-01-01", -1));
    }

    @Test
    void calculaElDiaDeLaSemana() {
        assertEquals(4, FechaIso.diaDeSemana("1970-01-01"));
        assertEquals(6, FechaIso.diaDeSemana("2026-11-21"));
        assertEquals(1, FechaIso.diaDeSemana("2026-11-23"));
        assertEquals(7, FechaIso.diaDeSemana("2026-11-22"));
    }
}
