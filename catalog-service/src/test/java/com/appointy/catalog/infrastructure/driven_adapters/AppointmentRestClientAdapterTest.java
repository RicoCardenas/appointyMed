package com.appointy.catalog.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.catalog.domain.exception.ConflictoCitasException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.infrastructure.config.AppointmentProperties;
import com.appointy.catalog.infrastructure.config.InternalProperties;
import com.appointy.catalog.infrastructure.driven_adapters.rest_client.AppointmentRestClientAdapter;
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

/** appointment simulado con el servidor HTTP del JDK: comprueba contrato, llave, correlacion, errores y timeouts. */
class AppointmentRestClientAdapterTest {

    static final String LLAVE = "llave-de-prueba-de-24-caracteres-o-mas";

    HttpServer servidor;
    int puerto;
    final AtomicReference<String> cuerpo = new AtomicReference<>();
    final AtomicReference<String> llaveRecibida = new AtomicReference<>();
    final AtomicReference<String> correlacionRecibida = new AtomicReference<>();
    final AtomicReference<String> tipoContenido = new AtomicReference<>();

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

    private AppointmentRestClientAdapter adaptador(int lecturaMs) {
        return new AppointmentRestClientAdapter(new AppointmentProperties("http://127.0.0.1:" + puerto, 500, lecturaMs), new InternalProperties(LLAVE));
    }

    private void responder(String ruta, int estado, String json) {
        servidor.createContext(ruta, ex -> {
            cuerpo.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            llaveRecibida.set(ex.getRequestHeaders().getFirst("X-Api-Key"));
            correlacionRecibida.set(ex.getRequestHeaders().getFirst("X-Correlation-Id"));
            tipoContenido.set(ex.getRequestHeaders().getFirst("Content-Type"));
            enviar(ex, estado, json);
        });
    }

    private static void enviar(HttpExchange ex, int estado, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(estado, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            ex.getResponseBody().write(bytes);
        }
        ex.close();
    }

    private final FranjaSolicitada[] franjas = {new FranjaSolicitada("2026-06-01", 480, 510)};

    @Test
    void generarFranjasEnviaElContratoYLaLlaveYDevuelveLosConteos() {
        responder("/internal/franjas/generar", 200, "{\"creadas\":5,\"existentes\":2}");

        ResultadoGeneracion r = adaptador(2000).generarFranjas("p1", "c1", franjas);

        assertEquals(new ResultadoGeneracion(5, 2), r);
        assertEquals(LLAVE, llaveRecibida.get());
        assertTrue(tipoContenido.get().startsWith("application/json"));
        assertTrue(cuerpo.get().contains("\"profesionalId\":\"p1\""), cuerpo.get());
        assertTrue(cuerpo.get().contains("\"fecha\":\"2026-06-01\""), cuerpo.get());
        assertTrue(cuerpo.get().contains("\"horaInicio\":\"08:00\""), cuerpo.get());
        assertTrue(cuerpo.get().contains("\"horaFin\":\"08:30\""), cuerpo.get());
    }

    @Test
    void propagaElIdDeCorrelacionDeLaPeticionEnCurso() {
        responder("/internal/franjas/generar", 200, "{\"creadas\":0,\"existentes\":0}");
        MDC.put("correlationId", "abc12345-corr");

        adaptador(2000).generarFranjas("p1", "c1", franjas);

        assertEquals("abc12345-corr", correlacionRecibida.get());
    }

    @Test
    void bloquearConRespuestaCorrectaNoLanzaNada() {
        responder("/internal/franjas/bloquear", 200, "{\"bloqueadas\":3}");

        assertDoesNotThrow(() -> adaptador(2000).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-15"));
        assertTrue(cuerpo.get().contains("\"fechaInicio\":\"2026-07-01\""), cuerpo.get());
        assertEquals(LLAVE, llaveRecibida.get());
    }

    @Test
    void unConflictoDeAppointmentSeConvierteEnLaListaDeCitasAfectadas() {
        responder("/internal/franjas/bloquear", 409, "{\"citasAfectadas\":[\"cita-1\",\"cita-2\"]}");

        ConflictoCitasException ex = assertThrows(ConflictoCitasException.class, () -> adaptador(2000).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-15"));

        assertArrayEquals(new String[] {"cita-1", "cita-2"}, ex.citasAfectadas());
    }

    @Test
    void unErrorDelServidorOLlaveRechazadaEsServicioNoDisponible() {
        responder("/internal/franjas/generar", 500, "{\"error\":\"x\"}");
        responder("/internal/franjas/bloquear", 401, "{}");

        assertEquals("APPOINTMENT_NO_DISPONIBLE", assertThrows(ServicioNoDisponibleException.class, () -> adaptador(2000).generarFranjas("p1", "c1", franjas)).codigo());
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(2000).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-02"));
    }

    @Test
    void conAppointmentCaidoNoSeCuelgaYSeInformaServicioNoDisponible() {
        servidor.stop(0);

        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(2000).generarFranjas("p1", "c1", franjas));
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(2000).bloquearFranjas("p1", "c1", "2026-07-01", "2026-07-02"));
    }

    @Test
    void unaRespuestaLentaSeCortaPorTimeout() {
        servidor.createContext("/internal/franjas/generar", ex -> {
            try {
                Thread.sleep(1500);
                enviar(ex, 200, "{\"creadas\":1,\"existentes\":0}");
            } catch (InterruptedException | IOException ignorada) {
                // el cliente ya cerro la conexion por timeout
            }
        });
        long inicio = System.nanoTime();

        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(300).generarFranjas("p1", "c1", franjas));

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(ms < 1200, "tardo " + ms + " ms: el timeout de lectura no se aplico");
    }
}
