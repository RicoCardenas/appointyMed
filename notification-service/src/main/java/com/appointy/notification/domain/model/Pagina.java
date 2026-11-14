package com.appointy.notification.domain.model;

/** Pagina de resultados; {@code pagina} empieza en 0. */
public record Pagina(Notificacion[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
