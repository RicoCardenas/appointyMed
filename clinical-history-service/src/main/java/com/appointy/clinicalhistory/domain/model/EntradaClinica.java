package com.appointy.clinicalhistory.domain.model;

/**
 * Entrada de la historia clinica. Inmutable (RF-29): una correccion es otra entrada con {@code corrigeAId} apuntando a la
 * anterior. {@code notas} e {@code indicaciones} pueden ser null. {@code creadoEn} en milisegundos desde 1970 (UTC).
 */
public record EntradaClinica(
        String id,
        String pacienteId,
        String profesionalId,
        String citaId,
        String motivo,
        String diagnostico,
        String notas,
        String indicaciones,
        String corrigeAId,
        long creadoEn) {}
