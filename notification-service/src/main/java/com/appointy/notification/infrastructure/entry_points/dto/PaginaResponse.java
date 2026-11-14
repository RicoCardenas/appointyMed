package com.appointy.notification.infrastructure.entry_points.dto;

import com.appointy.notification.domain.model.Pagina;
import java.util.Arrays;
import java.util.List;

public record PaginaResponse(List<NotificacionResponse> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static PaginaResponse de(Pagina p) {
        return new PaginaResponse(Arrays.stream(p.contenido()).map(NotificacionResponse::de).toList(), p.pagina(), p.tamano(),
                p.totalElementos(), p.totalPaginas());
    }
}
