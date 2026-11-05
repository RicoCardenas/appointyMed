package com.appointy.catalog.infrastructure.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Traduce los tipos simples del dominio (String, int, long) a los de las entidades. Las usan todos los mappers. */
@Component
public class Conversiones {

    public String idATexto(UUID id) {
        return id == null ? null : id.toString();
    }

    public UUID textoAId(String texto) {
        return texto == null ? null : UUID.fromString(texto);
    }

    public int horaAMinutos(LocalTime hora) {
        return hora.getHour() * 60 + hora.getMinute();
    }

    public LocalTime minutosAHora(int minutos) {
        return LocalTime.of(minutos / 60, minutos % 60);
    }

    public String fechaATexto(LocalDate fecha) {
        return fecha == null ? null : fecha.toString();
    }

    public LocalDate textoAFecha(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }

    public long instanteAMilisegundos(Instant instante) {
        return instante.toEpochMilli();
    }

    public Instant milisegundosAInstante(long milisegundos) {
        return Instant.ofEpochMilli(milisegundos);
    }
}
