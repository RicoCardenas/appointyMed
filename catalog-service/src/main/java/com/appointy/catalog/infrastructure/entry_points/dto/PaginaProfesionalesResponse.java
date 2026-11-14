package com.appointy.catalog.infrastructure.entry_points.dto;

import com.appointy.catalog.domain.model.PaginaProfesionales;
import java.util.Arrays;
import java.util.List;

public record PaginaProfesionalesResponse(List<ProfesionalResponse> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static PaginaProfesionalesResponse de(PaginaProfesionales p) {
        return new PaginaProfesionalesResponse(Arrays.stream(p.contenido()).map(ProfesionalResponse::de).toList(), p.pagina(), p.tamano(),
                p.totalElementos(), p.totalPaginas());
    }
}
