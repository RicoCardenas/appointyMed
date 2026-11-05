package com.appointy.appointment.infrastructure.driven_adapters.rest_client;

import com.appointy.appointment.domain.exception.ServicioNoDisponibleException;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.infrastructure.config.CatalogProperties;
import com.appointy.appointment.infrastructure.config.CorrelationIdFilter;
import com.appointy.appointment.infrastructure.config.InternalProperties;
import com.appointy.appointment.infrastructure.security.ApiKeyFilter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Llamadas /internal a catalog con llave de servicio y timeouts. Todo fallo de red, timeout, 5xx o llave rechazada
 * se informa como {@link ServicioNoDisponibleException} (503): nunca se deja pasar un cuelgue.
 */
@Component
public class CatalogRestClientAdapter implements CatalogClientPort {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogRestClientAdapter.class);
    private static final String CODIGO = "CATALOG_NO_DISPONIBLE";

    private final RestClient cliente;
    private final String llave;

    public CatalogRestClientAdapter(CatalogProperties propiedades, InternalProperties interna) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = interna.apiKey();
    }

    @Override
    public ParametrosCita obtenerParametros() {
        try {
            Map<String, String> valores = cliente.get().uri("/internal/parametros")
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(new ParameterizedTypeReference<Map<String, String>>() {});
            return new ParametrosCita(entero(valores, "anticipacion_minima_horas"), entero(valores, "limite_cancelacion_horas"),
                    entero(valores, "horizonte_max_dias"));
        } catch (RestClientResponseException e) {
            throw fallo("consultar los parametros", e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException e) {
            throw sinRespuesta("consultar los parametros", e);
        }
    }

    @Override
    public AsociacionProfesional validarAsociacion(String profesionalId, String especialidadId, String centroId) {
        try {
            Asociacion r = cliente.get()
                    .uri("/internal/profesionales/{id}/asociacion?especialidadId={e}&centroId={c}", profesionalId, especialidadId, centroId)
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(Asociacion.class);
            if (r == null) {
                throw new IllegalStateException("respuesta vacia");
            }
            return new AsociacionProfesional(r.profesionalActivo(), r.asociado());
        } catch (RestClientResponseException e) {
            throw fallo("validar la asociacion del profesional", e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException e) {
            throw sinRespuesta("validar la asociacion del profesional", e);
        }
    }

    @Override
    public ProfesionalDeUsuario buscarProfesionalDeUsuario(String usuarioId) {
        try {
            PerfilProfesional r = cliente.get().uri("/internal/profesionales/por-usuario/{id}", usuarioId)
                    .header(ApiKeyFilter.CABECERA, llave).header(CorrelationIdFilter.CABECERA, ClienteHttp.correlacion())
                    .retrieve().body(PerfilProfesional.class);
            if (r == null) {
                throw new IllegalStateException("respuesta vacia");
            }
            return new ProfesionalDeUsuario(r.profesionalId(), r.activo());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw fallo("buscar el profesional de la cuenta", e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException e) {
            throw sinRespuesta("buscar el profesional de la cuenta", e);
        }
    }

    private static int entero(Map<String, String> valores, String clave) {
        String valor = valores == null ? null : valores.get(clave);
        if (valor == null) {
            throw new IllegalStateException("falta el parametro " + clave);
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("parametro " + clave + " invalido");
        }
    }

    private static ServicioNoDisponibleException fallo(String operacion, int estado) {
        LOG.error("catalog respondio {} al {}", estado, operacion);
        return new ServicioNoDisponibleException(CODIGO, "El servicio de catalogo no pudo completar la operacion");
    }

    private static ServicioNoDisponibleException sinRespuesta(String operacion, Exception e) {
        LOG.error("catalog no respondio al {}: {}", operacion, e.getClass().getSimpleName());
        return new ServicioNoDisponibleException(CODIGO, "El servicio de catalogo no responde; intenta de nuevo mas tarde");
    }

    record Asociacion(boolean profesionalActivo, boolean asociado) {}

    record PerfilProfesional(String profesionalId, boolean activo) {}
}
