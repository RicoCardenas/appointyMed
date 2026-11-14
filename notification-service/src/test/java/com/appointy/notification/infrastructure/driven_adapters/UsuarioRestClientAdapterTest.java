package com.appointy.notification.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.infrastructure.config.AuthProperties;
import com.appointy.notification.infrastructure.config.InternalProperties;
import com.appointy.notification.infrastructure.driven_adapters.rest_client.UsuarioRestClientAdapter;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class UsuarioRestClientAdapterTest {

    static final String LLAVE = "llave-de-prueba-de-24-caracteres-o-mas";
    static final String ID = "33333333-3333-3333-3333-333333333333";

    HttpServer servidor;
    int puerto;
    final AtomicReference<String> llaveRecibida = new AtomicReference<>();
    final AtomicReference<String> correlacionRecibida = new AtomicReference<>();

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

    UsuarioRestClientAdapter adaptador(int lecturaMs) {
        return new UsuarioRestClientAdapter(new AuthProperties("http://127.0.0.1:" + puerto, 500, lecturaMs), new InternalProperties(LLAVE));
    }

    void responder(int estado, String cuerpo) {
        servidor.createContext("/internal/usuarios/" + ID + "/contacto", ex -> {
            llaveRecibida.set(ex.getRequestHeaders().getFirst("X-Api-Key"));
            correlacionRecibida.set(ex.getRequestHeaders().getFirst("X-Correlation-Id"));
            byte[] b = cuerpo.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(estado, b.length == 0 ? -1 : b.length);
            if (b.length > 0) ex.getResponseBody().write(b);
            ex.close();
        });
    }

    @Test
    void devuelveElCorreoYEnviaLlaveYCorrelacion() {
        responder(200, "{\"correo\":\"p@correo.co\"}");
        MDC.put("correlationId", "abc-12345678");
        assertEquals("p@correo.co", adaptador(500).correoDe(ID));
        assertEquals(LLAVE, llaveRecibida.get());
        assertEquals("abc-12345678", correlacionRecibida.get());
    }

    @Test
    void sinCorrelacionEnviaUnValorPorDefecto() {
        responder(200, "{\"correo\":\"p@correo.co\"}");
        adaptador(500).correoDe(ID);
        assertEquals("sin-correlacion", correlacionRecibida.get());
    }

    @Test
    void cuerpoVacioODestinatarioInexistenteDevuelveNull() {
        responder(404, "{}");
        assertNull(adaptador(500).correoDe(ID));
    }

    @Test
    void cincoCientosYLlaveRechazadaSonServicioNoDisponible() {
        responder(500, "{}");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).correoDe(ID));
        servidor.removeContext("/internal/usuarios/" + ID + "/contacto");
        responder(401, "{}");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).correoDe(ID));
    }

    @Test
    void servicioCaidoOTimeoutSonServicioNoDisponible() {
        servidor.createContext("/internal/usuarios/" + ID + "/contacto", ex -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            }
            ex.close();
        });
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).correoDe(ID));
        servidor.stop(0);
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).correoDe(ID));
    }
}
