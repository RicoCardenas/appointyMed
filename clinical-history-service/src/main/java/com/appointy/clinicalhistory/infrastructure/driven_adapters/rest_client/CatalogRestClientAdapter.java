package com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client;

import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;
import com.appointy.clinicalhistory.infrastructure.config.CatalogProperties;
import com.appointy.clinicalhistory.infrastructure.config.CorrelationIdFilter;
import com.appointy.clinicalhistory.infrastructure.config.InternalProperties;
import com.appointy.clinicalhistory.infrastructure.security.ApiKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Llamadas /internal a catalog con llave de servicio y timeouts. Todo fallo de red, timeout, 5xx o llave rechazada es un 503. */
@Component
public class CatalogRestClientAdapter implements CatalogClientGateway {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogRestClientAdapter.class);
    private static final String CODIGO = "CATALOG_NO_DISPONIBLE";

    private final RestClient cliente;
    private final String llave;

    public CatalogRestClientAdapter(CatalogProperties propiedades, InternalProperties interna) {
        this.cliente = ClienteHttp.construir(propiedades.baseUrl(), propiedades.connectTimeoutMs(), propiedades.readTimeoutMs());
        this.llave = interna.apiKey();
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
            LOG.error("catalog respondio {} al buscar el profesional de la cuenta", e.getStatusCode().value());
            throw new ServicioNoDisponibleException(CODIGO, "El servicio de catalogo no pudo completar la operacion");
        } catch (RestClientException | IllegalStateException e) {
            LOG.error("catalog no respondio al buscar el profesional de la cuenta: {}", e.getClass().getSimpleName());
            throw new ServicioNoDisponibleException(CODIGO, "El servicio de catalogo no responde; intenta de nuevo mas tarde");
        }
    }

    record PerfilProfesional(String profesionalId, boolean activo) {}
}
