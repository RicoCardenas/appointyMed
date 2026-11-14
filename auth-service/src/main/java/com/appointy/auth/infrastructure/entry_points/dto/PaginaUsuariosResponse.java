package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.PaginaUsuarios;
import java.util.Arrays;
import java.util.List;

public record PaginaUsuariosResponse(List<CuentaResponse> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static PaginaUsuariosResponse de(PaginaUsuarios p) {
        return new PaginaUsuariosResponse(Arrays.stream(p.contenido()).map(CuentaResponse::de).toList(), p.pagina(), p.tamano(),
                p.totalElementos(), p.totalPaginas());
    }
}
