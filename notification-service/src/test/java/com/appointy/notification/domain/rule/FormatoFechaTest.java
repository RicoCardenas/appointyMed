package com.appointy.notification.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatoFechaTest {

    @Test
    void fechaLargaEscribeDiaSemanaDiaMesYAnio() {
        assertEquals("miércoles 2 de diciembre de 2026", FormatoFecha.fechaLarga("2026-12-02"));
        assertEquals("viernes 20 de noviembre de 2026", FormatoFecha.fechaLarga("2026-11-20"));
        assertEquals("jueves 29 de febrero de 2024", FormatoFecha.fechaLarga("2024-02-29"));
        assertEquals("domingo 1 de enero de 2023", FormatoFecha.fechaLarga("2023-01-01"));
    }

    @Test
    void fechaCortaAbreviaDiaYMes() {
        assertEquals("mié 2 dic", FormatoFecha.fechaCorta("2026-12-02"));
        assertEquals("mar 8 dic", FormatoFecha.fechaCorta("2026-12-08"));
        assertEquals("sáb 5 sep", FormatoFecha.fechaCorta("2026-09-05"));
    }

    @Test
    void horaUsaFormatoDe12HorasConAmPm() {
        assertEquals("8:30 a. m.", FormatoFecha.hora("08:30"));
        assertEquals("12:00 p. m.", FormatoFecha.hora("12:00"));
        assertEquals("12:15 a. m.", FormatoFecha.hora("00:15"));
        assertEquals("5:05 p. m.", FormatoFecha.hora("17:05"));
    }

    @Test
    void codigoCortoToma8CaracteresEnMayuscula() {
        assertEquals("1FD9F85C", FormatoFecha.codigoCorto("1fd9f85c-e7b6-4405-bdcb-3c261d0fe77c"));
        assertEquals("AB", FormatoFecha.codigoCorto("ab"));
    }
}
