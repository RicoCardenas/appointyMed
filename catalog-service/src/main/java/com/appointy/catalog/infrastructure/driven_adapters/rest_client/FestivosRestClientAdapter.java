package com.appointy.catalog.infrastructure.driven_adapters.rest_client;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.infrastructure.config.FestivosProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * API externa Nager.Date (RA-06): GET /api/v3/PublicHolidays/{anio}/{pais}, sin llave. Con timeouts y cache por anio
 * (los festivos casi no cambian). Cualquier fallo se informa como {@link ServicioNoDisponibleException}.
 */
@Component
public class FestivosRestClientAdapter implements FestivosClientGateway {

    private final RestClient cliente;
    private final String pais;
    private final Cache<Integer, Festivo[]> cache;

    public FestivosRestClientAdapter(FestivosProperties propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(propiedades.connectTimeoutMs()));
        fabrica.setReadTimeout(Duration.ofMillis(propiedades.readTimeoutMs()));
        this.cliente = RestClient.builder().baseUrl(propiedades.baseUrl()).requestFactory(fabrica).build();
        this.pais = propiedades.pais();
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(propiedades.ttlHoras())).maximumSize(20).build();
    }

    @Override
    public Festivo[] festivos(int anio) {
        Festivo[] guardados = cache.getIfPresent(anio);
        if (guardados != null) {
            return guardados;
        }
        try {
            NagerFestivo[] respuesta = cliente.get().uri("/api/v3/PublicHolidays/{anio}/{pais}", anio, pais).retrieve().body(NagerFestivo[].class);
            if (respuesta == null) {
                throw new ServicioNoDisponibleException("FESTIVOS_NO_DISPONIBLES", "La API de festivos no devolvio datos");
            }
            Festivo[] festivos = new Festivo[respuesta.length];
            for (int i = 0; i < respuesta.length; i++) {
                festivos[i] = new Festivo(respuesta[i].date(), respuesta[i].localName());
            }
            cache.put(anio, festivos);
            return festivos;
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("FESTIVOS_NO_DISPONIBLES", "La API de festivos no respondio");
        }
    }

    record NagerFestivo(String date, String localName) {}
}
