package com.appointy.appointment.domain.model;

/** Pagina de citas; {@code pagina} empieza en 0. */
public record PaginaCitas(Cita[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
