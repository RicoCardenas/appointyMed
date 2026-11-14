package com.appointy.appointment.infrastructure.entry_points.dto;

import com.appointy.appointment.domain.model.PaginaCitasConFranja;
import java.util.Arrays;
import java.util.List;

public record PaginaCitasResponse(List<CitaResponse> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static PaginaCitasResponse de(PaginaCitasConFranja p) {
        return new PaginaCitasResponse(Arrays.stream(p.contenido()).map(CitaResponse::de).toList(), p.pagina(), p.tamano(), p.totalElementos(), p.totalPaginas());
    }
}
