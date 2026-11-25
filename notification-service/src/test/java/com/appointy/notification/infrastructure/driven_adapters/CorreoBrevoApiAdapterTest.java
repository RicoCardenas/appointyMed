package com.appointy.notification.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.notification.domain.exception.ServicioNoDisponibleException;
import com.appointy.notification.infrastructure.config.BrevoProperties;
import com.appointy.notification.infrastructure.driven_adapters.mail.CorreoBrevoApiAdapter;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class CorreoBrevoApiAdapterTest {

    static final String CLAVE = "clave-brevo-de-prueba";

    HttpServer servidor;
    int puerto;
    final AtomicReference<String> claveRecibida = new AtomicReference<>();
    final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    final AtomicReference<String> metodoRecibido = new AtomicReference<>();

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

    CorreoBrevoApiAdapter adaptador(int lecturaMs) {
        BrevoProperties propiedades = new BrevoProperties("http://127.0.0.1:" + puerto, CLAVE, 500, lecturaMs);
        return new CorreoBrevoApiAdapter(propiedades, new MockEnvironment().withProperty("app.mail.from", "clinica@appointy.test"));
    }

    void responder(int estado) {
        servidor.createContext("/v3/smtp/email", ex -> {
            metodoRecibido.set(ex.getRequestMethod());
            claveRecibida.set(ex.getRequestHeaders().getFirst("api-key"));
            cuerpoRecibido.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] b = "{\"messageId\":\"x\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(estado, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
    }

    @Test
    void enviaElCorreoPorHttpsConLaClaveYElRemitente() {
        responder(201);
        adaptador(500).enviar("paciente@correo.co", "Cita confirmada", "Tu cita es el lunes");
        assertEquals("POST", metodoRecibido.get());
        assertEquals(CLAVE, claveRecibida.get());
        assertTrue(cuerpoRecibido.get().contains("clinica@appointy.test"));
        assertTrue(cuerpoRecibido.get().contains("paciente@correo.co"));
        assertTrue(cuerpoRecibido.get().contains("Cita confirmada"));
        assertTrue(cuerpoRecibido.get().contains("Tu cita es el lunes"));
    }

    @Test
    void usaUnRemitentePorDefectoSiNoHayConfigurado() {
        responder(201);
        new CorreoBrevoApiAdapter(new BrevoProperties("http://127.0.0.1:" + puerto, CLAVE, 500, 500), new MockEnvironment())
                .enviar("a@b.co", "s", "c");
        assertTrue(cuerpoRecibido.get().contains("no-responder@appointy.local"));
    }

    @Test
    void unRechazoDeBrevoSeTraduceA503SinFiltrarDatos() {
        responder(401);
        ServicioNoDisponibleException e = assertThrows(ServicioNoDisponibleException.class,
                () -> adaptador(500).enviar("paciente@correo.co", "s", "cuerpo clinico"));
        assertEquals("CORREO_NO_DISPONIBLE", e.codigo());
        assertFalse(e.getMessage().contains("paciente@correo.co"));
        assertFalse(e.getMessage().contains("cuerpo clinico"));
    }

    @Test
    void unFalloDeRedSeTraduceA503() {
        servidor.stop(0);
        ServicioNoDisponibleException e = assertThrows(ServicioNoDisponibleException.class, () -> adaptador(500).enviar("a@b.co", "s", "c"));
        assertEquals("CORREO_NO_DISPONIBLE", e.codigo());
    }

    @Test
    void unaLecturaLentaSeTraduceA503() {
        servidor.createContext("/v3/smtp/email", ex -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            }
            ex.sendResponseHeaders(201, -1);
            ex.close();
        });
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador(200).enviar("a@b.co", "s", "c"));
    }
}
