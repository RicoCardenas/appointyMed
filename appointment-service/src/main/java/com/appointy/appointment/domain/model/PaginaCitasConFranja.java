package com.appointy.appointment.domain.model;

public record PaginaCitasConFranja(CitaConFranja[] contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
