package com.appointy.catalog.infrastructure.security;

import com.appointy.catalog.infrastructure.entry_points.dto.RespuestaError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Respuestas 401 y 403 que salen del filtro de seguridad con la misma estructura que el resto de errores. */
@Component
public class JsonErrorWriter {

    private final ObjectMapper mapper;

    public JsonErrorWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void escribir(HttpServletResponse response, int estado, String codigo, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), new RespuestaError(codigo, mensaje, List.of(), Instant.now()));
    }
}
