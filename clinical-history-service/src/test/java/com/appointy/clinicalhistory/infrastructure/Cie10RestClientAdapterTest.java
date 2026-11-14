package com.appointy.clinicalhistory.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.clinicalhistory.domain.exception.ServicioNoDisponibleException;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.infrastructure.config.Cie10Properties;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.rest_client.Cie10RestClientAdapter;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** La API externa CIE-10 simulada con el servidor HTTP del JDK: contrato, cache, errores y timeouts. */
class Cie10RestClientAdapterTest {

    HttpServer servidor;
    int puerto;
    final AtomicInteger llamadas = new AtomicInteger();
    final AtomicReference<String> consulta = new AtomicReference<>();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
        puerto = servidor.getAddress().getPort();
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    Cie10RestClientAdapter adaptador(int lecturaMs) {
        return new Cie10RestClientAdapter(new Cie10Properties("http://127.0.0.1:" + puerto, 500, lecturaMs, 1));
    }

    void responder(int estado, String cuerpo) {
        servidor.createContext("/api/icd10cm/v3/search", ex -> {
            llamadas.incrementAndGet();
            consulta.set(ex.getRequestURI().getRawQuery());
            byte[] b = cuerpo.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(estado, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
    }

    @Test
    void traduceLaRespuestaYUsaCache() {
        responder(200, "[2,[\"E11\",\"E10\"],null,[[\"E11\",\"Diabetes tipo 2\"],[\"E10\",\"Diabetes tipo 1\"]]]");
        Cie10RestClientAdapter a = adaptador(500);
        Diagnostico[] r = a.buscar("Diabetes mellitus", 15);
        assertEquals(2, r.length);
        assertEquals(new Diagnostico("E11", "Diabetes tipo 2"), r[0]);
        assertEquals(true, consulta.get().contains("terms=Diabetes%20mellitus") && consulta.get().contains("maxList=15") && consulta.get().contains("sf=code,name"));
        a.buscar("diabetes MELLITUS", 15);
        assertEquals(1, llamadas.get());
    }

    @Test
    void unaRespuestaInesperadaOUnErrorSonServicioNoDisponible() {
        responder(200, "{\"otra\":1}");
        assertEquals("CIE10_NO_DISPONIBLE", assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).buscar("gripe", 5)).codigo());
        servidor.removeContext("/api/icd10cm/v3/search");
        responder(500, "{}");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).buscar("gripe", 5));
        servidor.removeContext("/api/icd10cm/v3/search");
        responder(200, "null");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).buscar("gripe", 5));
    }

    @Test
    void timeoutYServicioCaido() {
        servidor.createContext("/api/icd10cm/v3/search", ex -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            }
            ex.close();
        });
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).buscar("gripe", 5));
        servidor.stop(0);
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).buscar("gripe", 5));
    }
}
