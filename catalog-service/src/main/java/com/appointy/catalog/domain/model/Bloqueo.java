package com.appointy.catalog.domain.model;

/** Fechas de calendario como texto ISO yyyy-MM-dd; {@code creadoEn} en milisegundos desde 1970 (UTC). */
public record Bloqueo(
        String id, String profesionalId, String centroId, String fechaInicio, String fechaFin, String motivo, String creadoPor, long creadoEn) {}
