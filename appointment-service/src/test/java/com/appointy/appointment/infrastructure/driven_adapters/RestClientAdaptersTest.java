package com.appointy.appointment.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.appointment.domain.exception.ServicioNoDisponibleException;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;
import com.appointy.appointment.infrastructure.config.CatalogProperties;
import com.appointy.appointment.infrastructure.config.InternalProperties;
import com.appointy.appointment.infrastructure.config.NotificationProperties;
import com.appointy.appointment.infrastructure.driven_adapters.rest_client.CatalogRestClientAdapter;
import com.appointy.appointment.infrastructure.driven_adapters.rest_client.NotificationRestClientAdapter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/** catalog y notification simulados con el servidor HTTP del JDK: contrato, llave, correlacion, errores y timeouts. */
class RestClientAdaptersTest {

    static final String LLAVE = "llave-de-prueba-de-24-caracteres-o-mas";

    HttpServer servidor;
    int puerto;
    final AtomicReference<String> cuerpo = new AtomicReference<>();
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

    CatalogRestClientAdapter catalog(int lecturaMs) {
        return new CatalogRestClientAdapter(new CatalogProperties("http://127.0.0.1:" + puerto, 500, lecturaMs), new InternalProperties(LLAVE));
    }

    NotificationRestClientAdapter notification() {
        return new NotificationRestClientAdapter(new NotificationProperties("http://127.0.0.1:" + puerto, 500, 500), new InternalProperties(LLAVE));
    }

    void responder(String ruta, int estado, String json) {
        servidor.createContext(ruta, ex -> {
            cuerpo.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
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
    void parametrosSeLeenConLlaveYCorrelacion() {
        MDC.put("correlationId", "corr-1234567");
        responder("/internal/parametros", 200,
                "{\"anticipacion_minima_horas\":\"2\",\"limite_cancelacion_horas\":\"24\",\"horizonte_max_dias\":\"60\",\"duracion_franja_min\":\"30\"}");

        ParametrosCita p = catalog(1000).obtenerParametros();

        assertEquals(new ParametrosCita(2, 24, 60), p);
        assertEquals(LLAVE, llaveRecibida.get());
        assertEquals("corr-1234567", correlacionRecibida.get());
    }

    @Test
    void parametrosIncompletosOInvalidosSonUn503() {
        responder("/internal/parametros", 200, "{\"anticipacion_minima_horas\":\"2\"}");
        assertThrows(ServicioNoDisponibleException.class, () -> catalog(1000).obtenerParametros());
        servidor.removeContext("/internal/parametros");
        responder("/internal/parametros", 200,
                "{\"anticipacion_minima_horas\":\"dos\",\"limite_cancelacion_horas\":\"24\",\"horizonte_max_dias\":\"60\"}");
        assertThrows(ServicioNoDisponibleException.class, () -> catalog(1000).obtenerParametros());
    }

    @Test
    void asociacionLlevaLosTresIdsEnLaConsulta() {
        responder("/internal/profesionales/", 200, "{\"profesionalActivo\":true,\"asociado\":false}");

        AsociacionProfesional r = catalog(1000).validarAsociacion("p1", "e1", "c1");

        assertTrue(r.profesionalActivo());
        assertFalse(r.asociado());
        assertEquals("/internal/profesionales/p1/asociacion?especialidadId=e1&centroId=c1", uriRecibida.get());
    }

    @Test
    void profesionalDeUsuarioDevuelveNullSiNoExiste() {
        responder("/internal/profesionales/por-usuario/u1", 200, "{\"profesionalId\":\"p9\",\"activo\":true}");
        responder("/internal/profesionales/por-usuario/u2", 404, "{\"codigo\":\"PROFESIONAL_NO_ENCONTRADO\"}");

        assertEquals(new ProfesionalDeUsuario("p9", true), catalog(1000).buscarProfesionalDeUsuario("u1"));
        assertNull(catalog(1000).buscarProfesionalDeUsuario("u2"));
    }

    @Test
    void errores5xxYLlaveRechazadaSonUn503() {
        responder("/internal/parametros", 500, "{}");
        assertEquals("CATALOG_NO_DISPONIBLE", assertThrows(ServicioNoDisponibleException.class, () -> catalog(1000).obtenerParametros()).codigo());
        servidor.removeContext("/internal/parametros");
        responder("/internal/parametros", 401, "{}");
        assertThrows(ServicioNoDisponibleException.class, () -> catalog(1000).obtenerParametros());
    }

    @Test
    void catalogCaidoOLentoSonUn503SinEsperarDeMas() {
        servidor.createContext("/internal/parametros", ex -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            enviar(ex, 200, "{}");
        });
        long inicio = System.currentTimeMillis();
        assertThrows(ServicioNoDisponibleException.class, () -> catalog(300).obtenerParametros());
        assertTrue(System.currentTimeMillis() - inicio < 2500, "el timeout de lectura debe cortar la espera");

        servidor.stop(0);
        assertThrows(ServicioNoDisponibleException.class, () -> catalog(300).obtenerParametros());
    }

    @Test
    void notificacionEnviaElEventoConLaHoraEnTextoYSinDatosPersonales() {
        responder("/internal/notificaciones", 202, "");

        notification().notificar(new EventoCita("CONFIRMADA", "cita1", "pac1", "prof1", "centro1", "2030-06-11", 485, "pac1"));

        assertEquals(LLAVE, llaveRecibida.get());
        assertTrue(cuerpo.get().contains("\"tipo\":\"CONFIRMADA\""));
        assertTrue(cuerpo.get().contains("\"horaInicio\":\"08:05\""));
        assertTrue(cuerpo.get().contains("\"fecha\":\"2030-06-11\""));
    }

    @Test
    void notificacionCaidaLanzaErrorQueElOyenteAtrapa() {
        responder("/internal/notificaciones", 500, "{}");
        assertThrows(ServicioNoDisponibleException.class,
                () -> notification().notificar(new EventoCita("CANCELADA", "c", "p", "pr", "ce", "2030-06-11", 480, "p")));
    }
}
