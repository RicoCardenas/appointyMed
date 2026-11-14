package com.appointy.auth.domain.model;

/** Pagina de resultados; {@code pagina} empieza en 0. */
public record PaginaUsuarios(Usuario[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
