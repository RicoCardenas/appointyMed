package com.appointy.appointment.infrastructure.driven_adapters.rest_client;

import com.appointy.appointment.infrastructure.config.CorrelationIdFilter;
import java.time.Duration;
import org.slf4j.MDC;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Construccion comun de los clientes /internal: timeouts de conexion y lectura (RNF-07). */
final class ClienteHttp {

    private ClienteHttp() {}

    static RestClient construir(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
    }

    static String correlacion() {
        String id = MDC.get(CorrelationIdFilter.CLAVE_MDC);
        return id == null ? "sin-correlacion" : id;
    }
}
