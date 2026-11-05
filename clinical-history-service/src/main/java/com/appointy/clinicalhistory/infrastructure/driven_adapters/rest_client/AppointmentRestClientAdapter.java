package com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client;

import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.model.CitaClinica;
import com.appointy.clinicalhistory.domain.model.EstadoCita;
import com.appointy.clinicalhistory.infrastructure.config.AppointmentProperties;
import com.appointy.clinicalhistory.infrastructure.config.CorrelationIdFilter;
import com.appointy.clinicalhistory.infrastructure.config.InternalProperties;
import com.appointy.clinicalhistory.infrastructure.security.ApiKeyFilter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Llamadas /internal a appointment con llave de servicio y timeouts; todo fallo es un 503. La relacion profesional-paciente
 * se guarda en cache (Caffeine) solo cuando existe: una negativa no se cachea para no ocultar una cita recien reservada.
 * El estado de la cita nunca se cachea.
 */
@Component
public class AppointmentRestClientAdapter implements AppointmentClientGateway {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentRestClientAdapter.class);
    private static final String CODIGO = "APPOINTMENT_NO_DISPONIBLE";

    private final RestClient cliente;
    private final String llave;
    private final Cache<String, Boolean> relaciones;

    public AppointmentRestClientAdapter(AppointmentProperties propiedades, InternalProperties interna) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = interna.apiKey();
        this.relaciones = Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofSeconds(propiedades.relacionesTtlSegundos())).build();
    }

    @Override
    public CitaClinica buscarCita(String citaId) {
        try {
            DatosCita r = cliente.get().uri("/internal/citas/{id}", citaId)
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(DatosCita.class);
            if (r == null || r.pacienteId() == null || r.profesionalId() == null || r.estado() == null) {
                throw new IllegalStateException("respuesta incompleta");
            }
            return new CitaClinica(citaId, r.pacienteId(), r.profesionalId(), EstadoCita.valueOf(r.estado()));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw fallo("consultar la cita", e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException | IllegalArgumentException e) {
            throw sinRespuesta("consultar la cita", e);
        }
    }

    @Override
    public boolean existeRelacion(String profesionalId, String pacienteId) {
        String clave = profesionalId + ":" + pacienteId;
        if (relaciones.getIfPresent(clave) != null) {
            return true;
        }
        try {
            Relacion r = cliente.get().uri("/internal/relaciones?profesionalId={p}&pacienteId={a}", profesionalId, pacienteId)
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(Relacion.class);
            if (r == null) {
                throw new IllegalStateException("respuesta vacia");
            }
            if (r.existe()) {
                relaciones.put(clave, Boolean.TRUE);
            }
            return r.existe();
        } catch (RestClientResponseException e) {
            throw fallo("validar la relacion profesional-paciente", e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException e) {
            throw sinRespuesta("validar la relacion profesional-paciente", e);
        }
    }

    private static ServicioNoDisponibleException fallo(String operacion, int estado) {
        LOG.error("appointment respondio {} al {}", estado, operacion);
        return new ServicioNoDisponibleException(CODIGO, "El servicio de citas no pudo completar la operacion");
    }

    private static ServicioNoDisponibleException sinRespuesta(String operacion, Exception e) {
        LOG.error("appointment no respondio al {}: {}", operacion, e.getClass().getSimpleName());
        return new ServicioNoDisponibleException(CODIGO, "El servicio de citas no responde; intenta de nuevo mas tarde");
    }

    record DatosCita(String citaId, String pacienteId, String profesionalId, String estado) {}

    record Relacion(boolean existe) {}
}
