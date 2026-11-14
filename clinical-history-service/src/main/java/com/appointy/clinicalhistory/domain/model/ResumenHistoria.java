package com.appointy.clinicalhistory.domain.model;

/** {@code atenciones}: entradas originales; {@code correcciones}: entradas que corrigen otra. {@code ultimaAtencionEn} es 0 si no hay entradas. */
public record ResumenHistoria(int atenciones, int correcciones, long ultimaAtencionEn, String ultimoDiagnostico) {}
