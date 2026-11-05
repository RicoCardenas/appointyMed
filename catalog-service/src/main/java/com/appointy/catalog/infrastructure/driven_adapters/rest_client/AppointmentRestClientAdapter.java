package com.appointy.catalog.infrastructure.driven_adapters.rest_client;

import com.appointy.catalog.domain.exception.ConflictoCitasException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.domain.port.out.AppointmentClientPort;
import com.appointy.catalog.infrastructure.config.AppointmentProperties;
import com.appointy.catalog.infrastructure.config.CorrelationIdFilter;
import com.appointy.catalog.infrastructure.config.InternalProperties;
import com.appointy.catalog.infrastructure.security.ApiKeyFilter;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Llamadas /internal a appointment-service con llave de servicio y timeouts (RNF-04, RNF-07). Cualquier fallo de red,
 * timeout o error del servidor se convierte en {@link ServicioNoDisponibleException}: nunca se deja pasar un cuelgue.
 */
@Component
public class AppointmentRestClientAdapter implements AppointmentClientPort {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentRestClientAdapter.class);

    private final RestClient cliente;
    private final String llave;

    public AppointmentRestClientAdapter(AppointmentProperties propiedades, InternalProperties interna) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(propiedades.connectTimeoutMs()));
        fabrica.setReadTimeout(Duration.ofMillis(propiedades.readTimeoutMs()));
        this.cliente = RestClient.builder().baseUrl(propiedades.baseUrl()).requestFactory(fabrica).build();
        this.llave = interna.apiKey();
    }

    @Override
    public ResultadoGeneracion generarFranjas(String profesionalId, String centroId, FranjaSolicitada[] franjas) {
        List<FranjaJson> cuerpo = Arrays.stream(franjas)
                .map(f -> new FranjaJson(f.fecha(), hora(f.horaInicio()), hora(f.horaFin())))
                .toList();
        try {
            RespuestaGeneracion r = cliente.post()
                    .uri("/internal/franjas/generar")
                    .header(ApiKeyFilter.CABECERA, llave)
                    .header(CorrelationIdFilter.CABECERA, correlacion())
                    .body(new SolicitudGeneracion(profesionalId, centroId, cuerpo))
                    .retrieve()
                    .body(RespuestaGeneracion.class);
            return new ResultadoGeneracion(r == null ? 0 : r.creadas(), r == null ? 0 : r.existentes());
        } catch (RestClientResponseException e) {
            throw fallo("generar franjas", e.getStatusCode().value());
        } catch (RestClientException e) {
            throw sinRespuesta("generar franjas", e);
        }
    }

    @Override
    public void bloquearFranjas(String profesionalId, String centroId, String fechaInicio, String fechaFin) {
        try {
            cliente.post()
                    .uri("/internal/franjas/bloquear")
                    .header(ApiKeyFilter.CABECERA, llave)
                    .header(CorrelationIdFilter.CABECERA, correlacion())
                    .body(new SolicitudBloqueo(profesionalId, centroId, fechaInicio, fechaFin))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                RespuestaConflicto conflicto = e.getResponseBodyAs(RespuestaConflicto.class);
                throw new ConflictoCitasException(conflicto == null || conflicto.citasAfectadas() == null
                        ? new String[0]
                        : conflicto.citasAfectadas().toArray(new String[0]));
            }
            throw fallo("bloquear franjas", e.getStatusCode().value());
        } catch (RestClientException e) {
            throw sinRespuesta("bloquear franjas", e);
        }
    }

    private static String hora(int minutos) {
        return String.format("%02d:%02d", minutos / 60, minutos % 60);
    }

    private static String correlacion() {
        String id = MDC.get(CorrelationIdFilter.CLAVE_MDC);
        return id == null ? "sin-correlacion" : id;
    }

    private static ServicioNoDisponibleException fallo(String operacion, int estado) {
        LOG.error("appointment respondio {} al pedir {}", estado, operacion);
        return new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "El servicio de citas no pudo completar la operacion");
    }

    private static ServicioNoDisponibleException sinRespuesta(String operacion, RestClientException e) {
        LOG.error("appointment no respondio al pedir {}: {}", operacion, e.getClass().getSimpleName());
        return new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "El servicio de citas no responde; intenta de nuevo mas tarde");
    }

    record FranjaJson(String fecha, String horaInicio, String horaFin) {}

    record SolicitudGeneracion(String profesionalId, String centroId, List<FranjaJson> franjas) {}

    record SolicitudBloqueo(String profesionalId, String centroId, String fechaInicio, String fechaFin) {}

    record RespuestaGeneracion(int creadas, int existentes) {}

    record RespuestaConflicto(List<String> citasAfectadas) {}
}
