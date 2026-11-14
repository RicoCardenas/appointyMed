package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import java.util.Arrays;
import java.util.List;

public record PaginaEntradasResponse(List<EntradaClinicaResponse> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static PaginaEntradasResponse de(PaginaEntradas p) {
        return new PaginaEntradasResponse(Arrays.stream(p.contenido()).map(EntradaClinicaResponse::de).toList(), p.pagina(), p.tamano(),
                p.totalElementos(), p.totalPaginas());
    }
}
