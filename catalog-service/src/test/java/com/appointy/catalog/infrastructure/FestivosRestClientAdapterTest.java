package com.appointy.catalog.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.infrastructure.config.FestivosProperties;
import com.appointy.catalog.infrastructure.driven_adapters.rest_client.FestivosRestClientAdapter;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** La API externa de festivos (Nager.Date) simulada con el servidor HTTP del JDK: contrato, cache, errores y timeouts. */
class FestivosRestClientAdapterTest {

    HttpServer servidor;
    int puerto;
    final AtomicInteger llamadas = new AtomicInteger();
    final AtomicReference<String> ruta = new AtomicReference<>();

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

    FestivosRestClientAdapter adaptador(int lecturaMs) {
        return new FestivosRestClientAdapter(new FestivosProperties("http://127.0.0.1:" + puerto, "CO", 500, lecturaMs, 1));
    }

    void responder(int estado, String cuerpo) {
        servidor.createContext("/api/v3/PublicHolidays", ex -> {
            llamadas.incrementAndGet();
            ruta.set(ex.getRequestURI().getPath());
            byte[] b = cuerpo.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(estado, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
    }

    @Test
    void traduceLaRespuestaYUsaCachePorAnio() {
        responder(200, "[{\"date\":\"2026-01-01\",\"localName\":\"Anio Nuevo\",\"name\":\"New Year\"},{\"date\":\"2026-01-12\",\"localName\":\"Reyes\",\"name\":\"Epiphany\"}]");
        FestivosRestClientAdapter a = adaptador(500);
        Festivo[] r = a.festivos(2026);
        assertEquals(2, r.length);
        assertEquals(new Festivo("2026-01-01", "Anio Nuevo"), r[0]);
        assertEquals("/api/v3/PublicHolidays/2026/CO", ruta.get());
        a.festivos(2026);
        assertEquals(1, llamadas.get());
        a.festivos(2027);
        assertEquals(2, llamadas.get());
    }

    @Test
    void errorDelServidorOCuerpoVacioSonServicioNoDisponible() {
        responder(500, "{}");
        assertEquals("FESTIVOS_NO_DISPONIBLES", assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).festivos(2026)).codigo());
        servidor.removeContext("/api/v3/PublicHolidays");
        responder(200, "null");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).festivos(2026));
    }

    @Test
    void timeoutYServicioCaido() {
        servidor.createContext("/api/v3/PublicHolidays", ex -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            }
            ex.close();
        });
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).festivos(2026));
        servidor.stop(0);
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).festivos(2026));
    }
}
