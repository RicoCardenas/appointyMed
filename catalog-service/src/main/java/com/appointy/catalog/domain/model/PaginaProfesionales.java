package com.appointy.catalog.domain.model;

/** Pagina de resultados; {@code pagina} empieza en 0. */
public record PaginaProfesionales(Profesional[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
