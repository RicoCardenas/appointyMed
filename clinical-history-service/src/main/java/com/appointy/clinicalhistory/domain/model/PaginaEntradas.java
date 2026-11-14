package com.appointy.clinicalhistory.domain.model;

/** Pagina de entradas; {@code pagina} empieza en 0. */
public record PaginaEntradas(EntradaClinica[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
