package com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client;

import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.gateway.Cie10ClientGateway;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.infrastructure.config.Cie10Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * API externa NLM Clinical Tables (RA-06): GET /api/icd10cm/v3/search?terms=..., sin llave. Respuesta: [total, codigos, null, [[codigo, nombre], ...]].
 * Solo sale el termino buscado (nunca ids ni datos del paciente) y no se registra. Cache por termino. Un fallo es {@link ServicioNoDisponibleException}.
 */
@Component
public class Cie10RestClientAdapter implements Cie10ClientGateway {

    private final RestClient cliente;
    private final Cache<String, Diagnostico[]> cache;

    public Cie10RestClientAdapter(Cie10Properties propiedades) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(propiedades.ttlHoras())).maximumSize(500).build();
    }

    @Override
    public Diagnostico[] buscar(String termino, int maximo) {
        String clave = termino.toLowerCase(Locale.ROOT) + "|" + maximo;
        Diagnostico[] guardados = cache.getIfPresent(clave);
        if (guardados != null) {
            return guardados;
        }
        try {
            JsonNode respuesta = cliente.get()
                    .uri(b -> b.path("/api/icd10cm/v3/search").queryParam("terms", "{t}").queryParam("maxList", maximo)
                            .queryParam("sf", "code,name").queryParam("df", "code,name").build(termino))
                    .retrieve().body(JsonNode.class);
            JsonNode filas = respuesta == null ? null : respuesta.get(3);
            if (filas == null || !filas.isArray()) {
                throw new ServicioNoDisponibleException("CIE10_NO_DISPONIBLE", "El catalogo CIE-10 devolvio una respuesta inesperada");
            }
            Diagnostico[] resultado = new Diagnostico[filas.size()];
            for (int i = 0; i < filas.size(); i++) {
                resultado[i] = new Diagnostico(filas.get(i).get(0).asText(), filas.get(i).get(1).asText());
            }
            cache.put(clave, resultado);
            return resultado;
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("CIE10_NO_DISPONIBLE", "El catalogo CIE-10 no respondio");
        }
    }
}
