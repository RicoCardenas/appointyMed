package com.appointy.clinicalhistory.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.model.CitaClinica;
import com.appointy.clinicalhistory.domain.model.EstadoCita;
import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;
import com.appointy.clinicalhistory.infrastructure.config.AppointmentProperties;
import com.appointy.clinicalhistory.infrastructure.config.CatalogProperties;
import com.appointy.clinicalhistory.infrastructure.config.InternalProperties;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client.AppointmentRestClientAdapter;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client.CatalogRestClientAdapter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/** catalog y appointment simulados con el servidor HTTP del JDK: contrato, llave, correlacion, cache, errores y timeouts. */
class RestClientAdaptersTest {

    static final String LLAVE = "llave-de-prueba-de-24-caracteres-o-mas";

    HttpServer servidor;
    int puerto;
    final AtomicInteger llamadas = new AtomicInteger();
    final AtomicReference<String> llaveRecibida = new AtomicReference<>();
    final AtomicReference<String> correlacionRecibida = new AtomicReference<>();
    final AtomicReference<String> uriRecibida = new AtomicReference<>();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
        puerto = servidor.getAddress().getPort();
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
        MDC.clear();
    }

    AppointmentRestClientAdapter appointment(int ttl, int lecturaMs) {
        return new AppointmentRestClientAdapter(new AppointmentProperties("http://127.0.0.1:" + puerto, 500, lecturaMs, ttl), new InternalProperties(LLAVE));
    }

    CatalogRestClientAdapter catalog() {
        return new CatalogRestClientAdapter(new CatalogProperties("http://127.0.0.1:" + puerto, 500, 500), new InternalProperties(LLAVE));
    }

    void responder(String ruta, int estado, String json) {
        servidor.createContext(ruta, ex -> {
            llamadas.incrementAndGet();
            llaveRecibida.set(ex.getRequestHeaders().getFirst("X-Api-Key"));
            correlacionRecibida.set(ex.getRequestHeaders().getFirst("X-Correlation-Id"));
            uriRecibida.set(ex.getRequestURI().toString());
            enviar(ex, estado, json);
        });
    }

    static void enviar(HttpExchange ex, int estado, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(estado, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            ex.getResponseBody().write(bytes);
        }
        ex.close();
    }

    @Test
    void laRelacionAfirmativaSeCacheaYLaNegativaNo() {
        responder("/internal/relaciones", 200, "{\"existe\":true}");
        AppointmentRestClientAdapter a = appointment(60, 500);
        assertTrue(a.existeRelacion("prof", "pac"));
        assertTrue(a.existeRelacion("prof", "pac"));
        assertEquals(1, llamadas.get());
        assertTrue(a.existeRelacion("prof", "otro"));
        assertEquals(2, llamadas.get());
        assertEquals("/internal/relaciones?profesionalId=prof&pacienteId=otro", uriRecibida.get());
        assertEquals(LLAVE, llaveRecibida.get());
    }

    @Test
    void laRelacionNegativaSiempreSeVuelveAPreguntar() {
        responder("/internal/relaciones", 200, "{\"existe\":false}");
        AppointmentRestClientAdapter a = appointment(60, 500);
        assertFalse(a.existeRelacion("prof", "pac"));
        assertFalse(a.existeRelacion("prof", "pac"));
        assertEquals(2, llamadas.get());
    }

    @Test
    void laCacheCaducaDespuesDelTtl() throws InterruptedException {
        responder("/internal/relaciones", 200, "{\"existe\":true}");
        AppointmentRestClientAdapter a = appointment(1, 500);
        a.existeRelacion("prof", "pac");
        Thread.sleep(1300);
        a.existeRelacion("prof", "pac");
        assertEquals(2, llamadas.get());
    }

    @Test
    void buscarCitaLeeIdsYEstadoYEnviaLlaveYCorrelacion() {
        MDC.put("correlationId", "corr-12345678");
        responder("/internal/citas/c1", 200, "{\"citaId\":\"c1\",\"pacienteId\":\"p\",\"profesionalId\":\"d\",\"estado\":\"COMPLETADA\"}");
        CitaClinica c = appointment(60, 500).buscarCita("c1");
        assertEquals(new CitaClinica("c1", "p", "d", EstadoCita.COMPLETADA), c);
        assertEquals(LLAVE, llaveRecibida.get());
        assertEquals("corr-12345678", correlacionRecibida.get());
    }

    @Test
    void buscarCitaNoCacheaElEstado() {
        responder("/internal/citas/c1", 200, "{\"citaId\":\"c1\",\"pacienteId\":\"p\",\"profesionalId\":\"d\",\"estado\":\"CONFIRMADA\"}");
        AppointmentRestClientAdapter a = appointment(60, 500);
        a.buscarCita("c1");
        a.buscarCita("c1");
        assertEquals(2, llamadas.get());
    }

    @Test
    void citaInexistenteDevuelveNull() {
        responder("/internal/citas/x", 404, "{}");
        assertNull(appointment(60, 500).buscarCita("x"));
    }

    @Test
    void errores5xxLlaveRechazadaYEstadoDesconocidoSonServicioNoDisponible() {
        responder("/internal/citas/a", 500, "{}");
        responder("/internal/citas/b", 401, "{}");
        responder("/internal/citas/c", 200, "{\"citaId\":\"c\",\"pacienteId\":\"p\",\"profesionalId\":\"d\",\"estado\":\"RARO\"}");
        responder("/internal/relaciones", 503, "{}");
        AppointmentRestClientAdapter a = appointment(60, 500);
        for (String id : new String[] {"a", "b", "c"}) {
            assertEquals("APPOINTMENT_NO_DISPONIBLE", assertThrows(ServicioNoDisponibleException.class, () -> a.buscarCita(id)).codigo());
        }
        assertThrows(ServicioNoDisponibleException.class, () -> a.existeRelacion("p", "q"));
    }

    @Test
    void servidorCaidoYTimeoutSonServicioNoDisponible() {
        servidor.createContext("/internal/relaciones", ex -> {
            try {
                Thread.sleep(1500);
                enviar(ex, 200, "{\"existe\":true}");
            } catch (InterruptedException | IOException e) {
                // el cliente ya cerro
            }
        });
        long inicio = System.currentTimeMillis();
        assertThrows(ServicioNoDisponibleException.class, () -> appointment(60, 300).existeRelacion("p", "q"));
        assertTrue(System.currentTimeMillis() - inicio < 1400);
        servidor.stop(0);
        assertThrows(ServicioNoDisponibleException.class, () -> appointment(60, 300).buscarCita("c"));
    }

    @Test
    void catalogDevuelveElProfesionalDeLaCuentaONullSiNoTienePerfil() {
        responder("/internal/profesionales/por-usuario/u1", 200, "{\"profesionalId\":\"prof-1\",\"activo\":true}");
        responder("/internal/profesionales/por-usuario/u2", 404, "{}");
        responder("/internal/profesionales/por-usuario/u3", 500, "{}");
        CatalogRestClientAdapter c = catalog();
        assertEquals(new ProfesionalDeUsuario("prof-1", true), c.buscarProfesionalDeUsuario("u1"));
        assertEquals(LLAVE, llaveRecibida.get());
        assertNull(c.buscarProfesionalDeUsuario("u2"));
        assertEquals("CATALOG_NO_DISPONIBLE", assertThrows(ServicioNoDisponibleException.class, () -> c.buscarProfesionalDeUsuario("u3")).codigo());
    }
}
